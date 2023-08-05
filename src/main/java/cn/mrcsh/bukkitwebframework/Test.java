package cn.mrcsh.bukkitwebframework;

import cn.mrcsh.bukkitwebframework.Config.WebConfig;
import cn.mrcsh.bukkitwebframework.Servlet.DispatcherServlet;
import cn.mrcsh.bukkitwebframework.Utils.LogUtils;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        Tomcat tomcat = new Tomcat();
        //构建Connector对象,此对象负责与客户端的连接.
        Connector con = new Connector("HTTP/1.1");
        //设置服务端的监听端口
        con.setPort(8081);
        //将Connector注册到service中
        tomcat.getService().addConnector(con);
        //注册servlet
        Context ctx = tomcat.addContext("", null);
        Tomcat.addServlet(ctx, "dispatcherServlet", new testServlet());
        //映射servlet
        ctx.addServletMappingDecoded("/", "dispatcherServlet");
        //启动tomcat
        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
        //阻塞当前线程
        tomcat.getServer().await();
    }

    static class testServlet extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            Map<String, String[]> parameterMap = req.getParameterMap();
            Class<testMethodClazz> clazz = testMethodClazz.class;
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                Parameter[] parameters = method.getParameters();
                for (Parameter parameter : parameters) {
                    System.out.println(parameter.getName());
                }
            }
            System.out.println("123");
        }

    }

    static class testMethodClazz{
        public void test(String str){

        }
    }
}
