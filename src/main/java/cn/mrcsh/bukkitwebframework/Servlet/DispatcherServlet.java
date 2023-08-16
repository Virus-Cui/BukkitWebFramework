package cn.mrcsh.bukkitwebframework.Servlet;


import cn.mrcsh.bukkitwebframework.Annotation.CrossOrigin;
import cn.mrcsh.bukkitwebframework.Annotation.FormParams;
import cn.mrcsh.bukkitwebframework.Annotation.MultiPartFile;
import cn.mrcsh.bukkitwebframework.Config.WebConfig;
import cn.mrcsh.bukkitwebframework.Enum.HTTPType;
import cn.mrcsh.bukkitwebframework.Enum.Mode;
import cn.mrcsh.bukkitwebframework.Module.FormDataModule;
import cn.mrcsh.bukkitwebframework.Module.MultipartFile;
import cn.mrcsh.bukkitwebframework.Module.RequestMethodMapping;
import cn.mrcsh.bukkitwebframework.Utils.FileUtils;
import com.alibaba.fastjson.JSON;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        if (req.getRequestURI().equals("/")) {
            if (!WebConfig.defaultPage.equals("")) {
                String[] split = WebConfig.defaultPage.split("/");
                for (String s : split) {
                    if (FileUtils.readFileToHttpServletResponse(resp, new File(WebConfig.staticBaseDir, s).getAbsolutePath())) {
                        return;
                    }
                }
            }
            returnDefaultDocument(resp);
            return;
        }

        // 选择的模式

        Mode mode = Mode.findMode(WebConfig.mode);
        try {
            switch (mode) {
                case WEB:
                    findByStatic(req, resp);
                    break;
                case BACKEND:
                    findByMethods(req, resp);
                    break;
                case MIXED:
                    boolean status = findByMethods(req, resp);
                    if (!status) {
                        findByStatic(req, resp);
                    }
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public void returnDefaultDocument(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        try {
            response.getWriter().write(String.format(WebConfig.defaultCallBackHTML, 404, "Page Is Not Found"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean findByMethods(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 设置返回字符集
        response.setCharacterEncoding("UTF-8");
        request.setCharacterEncoding("UTF-8");
        // 获取请求方式
        String method = request.getMethod().toUpperCase();
        // 获取请求url
        String url = request.getRequestURI();
        // 获取对应请求方式的方法集合
        LinkedHashMap<String, RequestMethodMapping> mappingMap = WebConfig.mappingHashMap.get(method);
        // 判断是否有该方式的方法
        if (mappingMap == null) {
            response.getWriter().write("method is not support");
            return true;
        }
        // 获取当前方法的映射对象
        RequestMethodMapping realMap = mappingMap.get(url);
        // 不是空，说明有这个请求方式的方法
        if (realMap != null) {
            // 设置返回Context类型
            response.setContentType("text/plain");
            CrossOrigin annotation = realMap.getMethod().getAnnotation(CrossOrigin.class);
            if (annotation != null) {

            }
            // 获取接口类对象
            Object o = realMap.getObj();
            // 定义参数数组
            Object[] param = null;
            // 获取请求query参数
            Map<String, String[]> requestParam = request.getParameterMap();
            // 获取映射的对象
            Method mappingMethod = realMap.getMethod();
            Object result = null;
            HTTPType type = HTTPType.findType(realMap.getType().getMethod());
            switch (type) {
                case GET:
                    if (mappingMethod.getParameters().length > 0) {
                        LinkedHashMap<String, Parameter> paramMap = realMap.getLinkedHashMap();
                        param = new Object[paramMap.size()];
                        for (Map.Entry<String, Parameter> entry : paramMap.entrySet()) {
                            int index = Integer.parseInt(entry.getValue().getName().replaceFirst("arg", ""));
                            // 映射HttpServletRequest
                            if (entry.getKey().equals("httpServletRequest")) {
                                param[index] = request;
                                continue;
                                // 映射HttpServletResponse
                            } else if (entry.getKey().equals("httpServletResponse")) {
                                param[index] = response;
                                continue;
                            }
                            param[index] = requestParam.get(entry.getKey())[0];
                        }
                    }
                    try {
                        result = param == null ? mappingMethod.invoke(o) : mappingMethod.invoke(o, param);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        response.getWriter().write(String.format(WebConfig.defaultCallBackHTML, e.getMessage()));
                        return true;
                    }
                    break;
                case POST:
                    // 从Request对象获取Form表单数据
                    FormDataModule formDataModule = read2Form(request);
                    // 获取Query和Body参数
                    // Query参数
                    Map<String, String[]> queryParams = null;
                    queryParams = request.getParameterMap();
                    // body参数
                    BufferedReader reader = null;
                    if (formDataModule == null) {
                        reader = request.getReader();
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    if(reader != null){
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                    }
                    String body = stringBuilder.toString();

                    // 获取属性映射关系
                    LinkedHashMap<String, Parameter> realParams = realMap.getLinkedHashMap();
                    param = new Object[realParams.size()];

                    for (Map.Entry<String, Parameter> entry : realParams.entrySet()) {
                        int index = Integer.parseInt(entry.getValue().getName().replaceFirst("arg", ""));
                        if (entry.getKey().equals("httpServletRequest")) {
                            param[index] = request;
                            continue;
                            // 映射HttpServletResponse
                        } else if (entry.getKey().equals("httpServletResponse")) {
                            param[index] = response;
                            continue;
                        }
                        if (queryParams != null && queryParams.containsKey(entry.getKey())) {
                            param[index] = queryParams.get(entry.getKey())[0];
                        } else if (formDataModule != null && entry.getValue().getAnnotation(FormParams.class) != null) {
                            FormParams formParam = entry.getValue().getAnnotation(FormParams.class);
                            param[index] = formDataModule.getSimpleParams().get(formParam.value());
                        } else if (formDataModule != null && entry.getValue().getAnnotation(MultiPartFile.class) != null) {
                            MultiPartFile file = entry.getValue().getAnnotation(MultiPartFile.class);
                            FileItem fileItem = formDataModule.getFileParams().get(file.value());
                            MultipartFile multipartFile = new MultipartFile();
                            multipartFile.setFileName(fileItem.getName());
                            multipartFile.setFileItem(fileItem);
                            param[index] = multipartFile;
                        } else {
                            param[index] = body;
                        }
                    }
                    try {
                        result = mappingMethod.invoke(o, param);
                    } catch (Exception e) {
                        response.getWriter().write(String.format(WebConfig.defaultCallBackHTML, e.getMessage()));
                        throw new RuntimeException(e);
                    }
                    break;
            }
            if (result == null) {
                return true;
            }
            if (result instanceof String) {
                response.getWriter().write((String) result);
                return true;
            }
            if (result instanceof Integer) {
                response.getWriter().write((Integer) result);
                return true;
            }
            response.getWriter().write(JSON.toJSONString(result));
            return true;
        }
        return false;
    }

    public void findByStatic(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean b = FileUtils.readFileToHttpServletResponse(response, new File(WebConfig.staticBaseDir, request.getRequestURI()).getAbsolutePath());
        if (!b) {
            returnDefaultDocument(response);
        }
    }

    public FormDataModule read2Form(HttpServletRequest request) throws Exception {
        FormDataModule formDataModule = new FormDataModule();
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        if (!request.getHeader("Content-Type").startsWith("multipart/")) {
            return null;
        }
        List<FileItem> list = upload.parseRequest(request);
        LinkedHashMap<String, String> simpleParams = new LinkedHashMap<>();
        for (FileItem item : list) {

            if (item.isFormField()) {
                //得到的是普通输入项
                String name = item.getFieldName();  //得到输入项的名称
                String value = item.getString();
                simpleParams.put(name, value);
            } else {
                String filename = item.getFieldName();
                filename = filename.substring(filename.lastIndexOf("\\") + 1);
                formDataModule.fileParams.put(filename, item);
            }
        }
        formDataModule.setSimpleParams(simpleParams);
        return formDataModule;
    }
}
