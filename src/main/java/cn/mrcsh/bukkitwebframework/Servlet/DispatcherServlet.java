package cn.mrcsh.bukkitwebframework.Servlet;


import cn.mrcsh.bukkitwebframework.Config.WebConfig;
import cn.mrcsh.bukkitwebframework.Module.RequestMethodMapping;
import cn.mrcsh.bukkitwebframework.Utils.FileUtils;
import cn.mrcsh.bukkitwebframework.Utils.LogUtils;
import com.alibaba.fastjson.JSON;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(req.getRequestURI().equals("/")){
            returnDefaultDocument(resp);
            return;
        }

        String method = req.getMethod().toUpperCase();
        String url = req.getRequestURI();
        LinkedHashMap<String, RequestMethodMapping> mappingMap = WebConfig.mappingHashMap.get(method);
        if(mappingMap == null){
            resp.getWriter().write("method is not support");
            return;
        }
        RequestMethodMapping realMap = mappingMap.get(url);
        if(realMap != null){
            resp.setContentType("text/plain");
            Object o = realMap.getObj();
            Object [] param = null;
            Map<String, String[]> requestParam = req.getParameterMap();
            Method mappingMethod = realMap.getMethod();
            if(mappingMethod.getParameters().length>0){
                param = new Object[mappingMethod.getParameters().length];
                LinkedHashMap<String, String> paramMap = realMap.getLinkedHashMap();
                for (Map.Entry<String, String[]> entry : requestParam.entrySet()) {
                    String key = entry.getKey();
                    String s = paramMap.get(key);
                    s = s.replaceAll("arg","");
                    int num = Integer.parseInt(s);
                    param[num] = entry.getValue()[0];
                }
            }
            try {
                Object invoke = param==null?mappingMethod.invoke(o):mappingMethod.invoke(o,param);
                if(invoke == null){
                    return;
                }
                if(invoke instanceof String){
                    resp.getWriter().write((String) invoke);
                    return;
                }
                if(invoke instanceof Integer){
                    resp.getWriter().write((Integer) invoke);
                    return;
                }
                resp.getWriter().write(JSON.toJSONString(invoke));
                return;
            } catch (IllegalAccessException | InvocationTargetException e) {
                LogUtils.error("&s 执行方法出错",e);
            }
        }
        boolean b = FileUtils.readFileToHttpServletResponse(resp, new File(WebConfig.staticBaseDir, req.getRequestURI()).getAbsolutePath());
        if(!b){
            returnDefaultDocument(resp);
        }
    }

    public void returnDefaultDocument(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.getWriter().write(String.format(WebConfig.defaultCallBackHTML,404,"Page Is Not Found"));
    }
}
