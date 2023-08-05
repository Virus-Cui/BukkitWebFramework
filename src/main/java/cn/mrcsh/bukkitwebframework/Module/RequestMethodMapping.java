package cn.mrcsh.bukkitwebframework.Module;

import cn.mrcsh.bukkitwebframework.Enum.HTTPType;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;

public class RequestMethodMapping {
    private HTTPType type;
    private Method method;
    private String name;
    private Object obj;
    // key: 变量名 value: 反射获取的变量名
    private LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();

    public RequestMethodMapping() {
    }

    public RequestMethodMapping(HTTPType type, Method method, String name) {
        this.type = type;
        this.method = method;
        this.name = name;
    }

    public HTTPType getType() {
        return type;
    }

    public RequestMethodMapping setType(HTTPType type) {
        this.type = type;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public RequestMethodMapping setMethod(Method method) {
        this.method = method;
        return this;
    }

    public String getName() {
        return name;
    }

    public RequestMethodMapping setName(String name) {
        this.name = name;
        return this;
    }

    public Object getObj() {
        return obj;
    }

    public RequestMethodMapping setObj(Object obj) {
        this.obj = obj;
        return this;
    }

    public LinkedHashMap<String, String> getLinkedHashMap() {
        return linkedHashMap;
    }

    public RequestMethodMapping setLinkedHashMap(LinkedHashMap<String, String> linkedHashMap) {
        this.linkedHashMap = linkedHashMap;
        return this;
    }

    @Override
    public String toString() {
        return "RequestMethodMapping{" +
                "type=" + type +
                ", method=" + method +
                ", name='" + name + '\'' +
                '}';
    }
}
