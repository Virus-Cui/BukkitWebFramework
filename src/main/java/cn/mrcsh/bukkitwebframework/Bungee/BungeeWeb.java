package cn.mrcsh.bukkitwebframework.Bungee;

import cn.mrcsh.bukkitwebframework.Annotation.*;
import cn.mrcsh.bukkitwebframework.Config.WebConfig;
import cn.mrcsh.bukkitwebframework.Container.WebContainer;
import cn.mrcsh.bukkitwebframework.Enum.HTTPType;
import cn.mrcsh.bukkitwebframework.Module.RequestMethodMapping;
import cn.mrcsh.bukkitwebframework.Utils.ClassUtil;
import cn.mrcsh.bukkitwebframework.Servlet.DispatcherServlet;
import cn.mrcsh.bukkitwebframework.Bungee.Utils.ConfigYmlUtil;
import cn.mrcsh.bukkitwebframework.Bungee.Utils.LogUtils;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.LinkedHashMap;

public class BungeeWeb {

    public static void initialization(Class<? extends Plugin> clazz, Plugin plugin, boolean nolog) {
        LogUtils.noLog = nolog;
        LogUtils.plugin = plugin;
        LogUtils.info("&6  ______     __  __     __   __     ______     ______     ______     __     __     ______     ______");
        LogUtils.info("&6 /\\  == \\   /\\ \\/\\ \\   /\\ \"-.\\ \\   /\\  ___\\   /\\  ___\\   /\\  ___\\   /\\ \\  _ \\ \\   /\\  ___\\   /\\  == \\");
        LogUtils.info("&6 \\ \\  __<   \\ \\ \\_\\ \\  \\ \\ \\-.  \\  \\ \\ \\__ \\  \\ \\  __\\   \\ \\  __\\   \\ \\ \\/ \".\\ \\  \\ \\  __\\   \\ \\  __<");
        LogUtils.info("&6  \\ \\_____\\  \\ \\_____\\  \\ \\_\\\\\"\\_\\  \\ \\_____\\  \\ \\_____\\  \\ \\_____\\  \\ \\__/\".~\\_\\  \\ \\_____\\  \\ \\_____\\");
        LogUtils.info("&6   \\/_____/   \\/_____/   \\/_/ \\/_/   \\/_____/   \\/_____/   \\/_____/   \\/_/   \\/_/   \\/_____/   \\/_____/");
        // 加载配置文件
        loadConfig(plugin);
        // 映射请求路径
        try {
            mappingRequestUrl(clazz, plugin);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LogUtils.error("映射Controller失败",e);
            return;
        }

        // 初始化Tomcat
        new Thread(()->{
            WebContainer.initWebServer(new DispatcherServlet(), plugin.getDataFolder().getAbsolutePath());
        }, "Web Thread").start();
    }

    private static void initWebFolder(Plugin plugin) {
        ConfigYmlUtil.saveResource(plugin,"config.yml");
    }

    private static void loadConfig(Plugin plugin) {
        initWebFolder(plugin);
        Configuration configuration = ConfigYmlUtil.getYamlConfiguration(plugin,"web/config.yml");
        WebConfig.serverPort = configuration.getInt("server.port");
        LogUtils.info("&6服务端口:" + WebConfig.serverPort);
        WebConfig.mode = configuration.getString("mode");
        LogUtils.info("&6服务模式:" + WebConfig.mode);
        WebConfig.staticBaseDir = plugin.getDataFolder().getAbsolutePath()+File.separator+"web"+ File.separator+configuration.getString("static.base.dir").replaceFirst("/","");
        LogUtils.info("&6静态文件主目录:" + WebConfig.staticBaseDir);
        WebConfig.defaultPage = configuration.getString("default.page");
        LogUtils.info("&6默认文件:" + WebConfig.defaultPage);

    }

    private static void mappingRequestUrl(Class<? extends Plugin> clazz, Plugin plugin)  throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
        LogUtils.info("&6initialization DispatcherServlet...");
        String basePackageName = clazz.getPackage().getName();
        Collection<Class<?>> classesInPackage = ClassUtil.getClassesInPackage(plugin.getClass().getProtectionDomain().getCodeSource(), basePackageName);
        for (Class<?> ControllerClazz : classesInPackage) {
            if(ControllerClazz.getAnnotation(Controller.class) != null){
                for (Method method : ControllerClazz.getDeclaredMethods()) {
                    if(method.getAnnotation(GetMapping.class) != null){
                        GetMapping annotation = method.getAnnotation(GetMapping.class);
                        Parameter[] parameters = method.getParameters();
                        RequestMethodMapping requestMethodMapping = new RequestMethodMapping();
                        requestMethodMapping.setMethod(method);
                        requestMethodMapping.setType(HTTPType.GET);
                        requestMethodMapping.setName(annotation.value());
                        requestMethodMapping.setObj(ControllerClazz.getDeclaredConstructor().newInstance());
                        if(parameters.length > 0){
                            LinkedHashMap<String, Parameter> linkedHashMap = new LinkedHashMap<>();
                            for (Parameter parameter : parameters) {
                                if(parameter.getType() == HttpServletRequest.class){
                                    linkedHashMap.put("httpServletRequest", parameter);
                                    continue;
                                }
                                if(parameter.getType() == HttpServletResponse.class){
                                    linkedHashMap.put("httpServletResponse", parameter);
                                    continue;
                                }
                                RequestParam param = parameter.getAnnotation(RequestParam.class);
                                linkedHashMap.put(param.value(), parameter);
                            }
                            requestMethodMapping.setLinkedHashMap(linkedHashMap);
                        }
                        WebConfig.mappingHashMap.get("GET").put(annotation.value(), requestMethodMapping);
                    }else if(method.getAnnotation(PostMapping.class) != null){
                        PostMapping annotation = method.getAnnotation(PostMapping.class);
                        RequestMethodMapping requestMethodMapping = new RequestMethodMapping();
                        requestMethodMapping.setMethod(method);
                        requestMethodMapping.setType(HTTPType.POST);
                        requestMethodMapping.setName(annotation.value());
                        requestMethodMapping.setObj(ControllerClazz.getDeclaredConstructor().newInstance());
                        Parameter[] parameters = method.getParameters();
                        if(parameters.length > 0){
                            LinkedHashMap<String ,Parameter> linkedHashMap = new LinkedHashMap<>();
                            for (Parameter parameter : parameters) {
                                if(parameter.getType() == HttpServletRequest.class){
                                    linkedHashMap.put("httpServletRequest", parameter);
                                    continue;
                                }
                                if(parameter.getType() == HttpServletResponse.class){
                                    linkedHashMap.put("httpServletResponse", parameter);
                                    continue;
                                }
                                RequestParam queryParameter = parameter.getAnnotation(RequestParam.class);
                                RequestBody body = parameter.getAnnotation(RequestBody.class);
                                FormParam formParam = parameter.getAnnotation(FormParam.class);
                                MultiPartFile multiPartFile = parameter.getAnnotation(MultiPartFile.class);
                                if(body != null){
                                    linkedHashMap.put("body",parameter);
                                } else if(formParam != null){
                                    linkedHashMap.put(formParam.value(), parameter);
                                }else if(multiPartFile != null){
                                    linkedHashMap.put(multiPartFile.value(), parameter);
                                } else {
                                    linkedHashMap.put(queryParameter.value(),parameter);
                                }
                            }
                            requestMethodMapping.setLinkedHashMap(linkedHashMap);
                        }
                        WebConfig.mappingHashMap.get("POST").put(annotation.value(), requestMethodMapping);
                    }else if(method.getAnnotation(RequestMapping.class) != null){
                        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                        RequestMethodMapping requestMethodMapping = new RequestMethodMapping();
                        requestMethodMapping.setMethod(method);
                        requestMethodMapping.setType(annotation.method());
                        requestMethodMapping.setName(annotation.path());
                        requestMethodMapping.setObj(ControllerClazz.getDeclaredConstructor().newInstance());
                        WebConfig.mappingHashMap.get("GET").put(annotation.path(), requestMethodMapping);
                    }
                }
            }
        }

        System.out.println(WebConfig.mappingHashMap);
        LogUtils.info("&6initialization DispatcherServlet completed");
    }

    class Config{
        public void noLog(){

        }
    }

}
