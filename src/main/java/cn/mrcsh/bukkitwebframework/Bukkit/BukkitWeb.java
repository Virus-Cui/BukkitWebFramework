package cn.mrcsh.bukkitwebframework.Bukkit;

import cn.mrcsh.bukkitwebframework.Bukkit.Annotation.*;
import cn.mrcsh.bukkitwebframework.Bukkit.Config.WebConfig;
import cn.mrcsh.bukkitwebframework.Bukkit.Enum.HTTPType;
import cn.mrcsh.bukkitwebframework.Bukkit.Module.RequestMethodMapping;
import cn.mrcsh.bukkitwebframework.Bukkit.Utils.ClassUtil;
import cn.mrcsh.bukkitwebframework.Bukkit.Utils.ConfigUtils;
import cn.mrcsh.bukkitwebframework.Bukkit.Utils.LogUtils;
import cn.mrcsh.bukkitwebframework.Bungee.Servlet.DispatcherServlet;
import cn.mrcsh.bukkitwebframework.Container.WebContainer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.LinkedHashMap;

public class BukkitWeb {

    public static void initialization(Class<? extends JavaPlugin> clazz,JavaPlugin plugin, boolean nolog) {
        LogUtils.noLog = nolog;
        LogUtils.info("&6 ______     __  __     __  __     __  __     __     ______   __     __     ______     ______    ");
        LogUtils.info("&6/\\  == \\   /\\ \\/\\ \\   /\\ \\/ /    /\\ \\/ /    /\\ \\   /\\__  _\\ /\\ \\  _ \\ \\   /\\  ___\\   /\\  == \\   ");
        LogUtils.info("&6\\ \\  __<   \\ \\ \\_\\ \\  \\ \\  _\"-.  \\ \\  _\"-.  \\ \\ \\  \\/_/\\ \\/ \\ \\ \\/ \".\\ \\  \\ \\  __\\   \\ \\  __<   ");
        LogUtils.info("&6 \\ \\_____\\  \\ \\_____\\  \\ \\_\\ \\_\\  \\ \\_\\ \\_\\  \\ \\_\\    \\ \\_\\  \\ \\__/\".~\\_\\  \\ \\_____\\  \\ \\_____\\");
        LogUtils.info("&6  \\/_____/   \\/_____/   \\/_/\\/_/   \\/_/\\/_/   \\/_/     \\/_/   \\/_/   \\/_/   \\/_____/   \\/_____/");
        // 初始化Bukkit容器
        ConfigUtils.javaPlugin = plugin;
        // 加载配置文件
        loadConfig();
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

    private static void initWebFolder() {
        ConfigUtils.save("web/config.yml", false);
    }

    private static void loadConfig() {
        initWebFolder();
        YamlConfiguration configuration = ConfigUtils.loadConfig("web/config.yml");
        WebConfig.serverPort = configuration.getInt("server.port");
        LogUtils.info("&6服务端口:" + WebConfig.serverPort);
        WebConfig.mode = configuration.getString("mode");
        LogUtils.info("&6服务模式:" + WebConfig.mode);
        WebConfig.staticBaseDir = ConfigUtils.javaPlugin.getDataFolder().getAbsolutePath()+File.separator+"web"+ File.separator+configuration.getString("static.base.dir").replaceFirst("/","");
        LogUtils.info("&6静态文件主目录:" + WebConfig.staticBaseDir);
        WebConfig.defaultPage = configuration.getString("default.page");
        LogUtils.info("&6默认文件:" + WebConfig.defaultPage);

    }

    private static void mappingRequestUrl(Class<? extends JavaPlugin> clazz, JavaPlugin plugin)  throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
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
                            LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
                            for (Parameter parameter : parameters) {
                                if(parameter.getType() == HttpServletRequest.class){
                                    linkedHashMap.put("httpServletRequest", parameter.getName());
                                    continue;
                                }
                                if(parameter.getType() == HttpServletResponse.class){
                                    linkedHashMap.put("httpServletResponse", parameter.getName());
                                    continue;
                                }
                                RequestParam param = parameter.getAnnotation(RequestParam.class);
                                linkedHashMap.put(param.value(), parameter.getName());
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
                            LinkedHashMap<String ,String> linkedHashMap = new LinkedHashMap<>();
                            for (Parameter parameter : parameters) {
                                if(parameter.getType() == HttpServletRequest.class){
                                    linkedHashMap.put("httpServletRequest", parameter.getName());
                                    continue;
                                }
                                if(parameter.getType() == HttpServletResponse.class){
                                    linkedHashMap.put("httpServletResponse", parameter.getName());
                                    continue;
                                }
                                RequestParam queryParameter = parameter.getAnnotation(RequestParam.class);
                                RequestBody body = parameter.getAnnotation(RequestBody.class);
                                if(body != null){
                                    linkedHashMap.put("body",parameter.getName());
                                }else {
                                    linkedHashMap.put(queryParameter.value(),parameter.getName());
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
        LogUtils.info("&6initialization DispatcherServlet completed");
    }

    class Config{
        public void noLog(){

        }
    }

}
