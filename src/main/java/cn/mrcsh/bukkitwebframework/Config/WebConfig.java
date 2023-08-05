package cn.mrcsh.bukkitwebframework.Config;

import cn.mrcsh.bukkitwebframework.Module.RequestMethodMapping;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class WebConfig {
    public static String defaultCallBackHTML = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Document</title>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "    <h1>Error</h1>\n" +
            "    <h3>error code is %s</h3>\n" +
            "    <h4>%s</h4>\n" +
            "</body>\n" +
            "\n" +
            "</html>";
    public static LinkedHashMap<String, LinkedHashMap<String, RequestMethodMapping>> mappingHashMap = new LinkedHashMap<>();

    static {
        mappingHashMap.put("GET",new LinkedHashMap<String, RequestMethodMapping>());
        mappingHashMap.put("POST",new LinkedHashMap<String, RequestMethodMapping>());
    }

    public static Integer serverPort = 8080;
    public static String staticBaseDir = "";
}