package cn.mrcsh.bukkitwebframework.Bukkit.Servlet;


import cn.mrcsh.bukkitwebframework.Bukkit.Annotation.CrossOrigin;
import cn.mrcsh.bukkitwebframework.Bukkit.Config.WebConfig;
import cn.mrcsh.bukkitwebframework.Bukkit.Enum.HTTPType;
import cn.mrcsh.bukkitwebframework.Bukkit.Enum.Mode;
import cn.mrcsh.bukkitwebframework.Bukkit.Module.RequestMethodMapping;
import cn.mrcsh.bukkitwebframework.Bukkit.Utils.FileUtils;
import com.alibaba.fastjson.JSON;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getRequestURI().equals("/")) {
            if(!WebConfig.defaultPage.equals("")){
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

    }

    public void returnDefaultDocument(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.getWriter().write(String.format(WebConfig.defaultCallBackHTML, 404, "Page Is Not Found"));
    }

    public boolean findByMethods(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
            if(annotation != null){

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
                        LinkedHashMap<String, String> paramMap = realMap.getLinkedHashMap();
                        param = new Object[paramMap.size()];
                        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                            int index = Integer.parseInt(entry.getValue().replaceFirst("arg",""));
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
                    // 获取Query和Body参数
                    // Query参数
                    Map<String, String[]> queryParams = request.getParameterMap();
                    // body参数
                    BufferedReader reader = request.getReader();
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    String body = stringBuilder.toString();

                    // 获取属性映射关系
                    LinkedHashMap<String, String> realParams = realMap.getLinkedHashMap();
                    param = new Object[realParams.size()];

                    for (Map.Entry<String, String> entry : realParams.entrySet()) {
                        int index = Integer.parseInt(entry.getValue().replaceFirst("arg", ""));
                        if (entry.getKey().equals("httpServletRequest")) {
                            param[index] = request;
                            continue;
                            // 映射HttpServletResponse
                        } else if (entry.getKey().equals("httpServletResponse")) {
                            param[index] = response;
                            continue;
                        }
                        if (queryParams.containsKey(entry.getKey())) {
                            param[index] = queryParams.get(entry.getKey())[0];
                        } else {
                            param[index] = body;
                        }
                    }
                    try {
                        result = mappingMethod.invoke(o, param);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        response.getWriter().write(String.format(WebConfig.defaultCallBackHTML, e.getMessage()));
                        return true;
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
}