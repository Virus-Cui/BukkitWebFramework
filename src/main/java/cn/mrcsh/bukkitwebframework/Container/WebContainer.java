package cn.mrcsh.bukkitwebframework.Container;

import cn.mrcsh.bukkitwebframework.Bungee.Utils.LogUtils;
import cn.mrcsh.bukkitwebframework.Config.WebConfig;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.http.HttpServlet;

public class WebContainer {
    private static Tomcat webServer;


    public static Tomcat getWebServerInstance(){
        return webServer;
    }

    public static void initWebServer(HttpServlet dispatcherServlet, String baseDir){
        new Thread(()->{
            LogUtils.info("&6开始初始化Tomcat Web服务器");
            Tomcat tomcat = new Tomcat();
            //构建Connector对象,此对象负责与客户端的连接.
            Connector con = new Connector("HTTP/1.1");
            //设置服务端的监听端口
            con.setPort(WebConfig.serverPort);
            //将Connector注册到service中
            tomcat.getService().addConnector(con);
            //注册servlet
            Context ctx = tomcat.addContext("", null);
            Tomcat.addServlet(ctx, "dispatcherServlet", dispatcherServlet);
            //映射servlet
            ctx.addServletMappingDecoded("/", "dispatcherServlet");
            //启动tomcat
            tomcat.setBaseDir(baseDir);
            try {
                tomcat.start();
            } catch (LifecycleException e) {
                throw new RuntimeException(e);
            }
            //阻塞当前线程
            LogUtils.info("&6Tomcat 初始化完成");
            webServer = tomcat;
            tomcat.getServer().await();
        }).start();
    }
}
