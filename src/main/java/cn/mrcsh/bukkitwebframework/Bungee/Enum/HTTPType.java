package cn.mrcsh.bukkitwebframework.Bungee.Enum;

public enum HTTPType {
    GET("GET","GET请求"),
    POST("POST","POST请求")
    ;
    private String method;
    private String desc;

    HTTPType(String method, String desc) {
        this.method = method;
        this.desc = desc;
    }

    public String getMethod() {
        return method;
    }

    public String getDesc() {
        return desc;
    }

    public static HTTPType findType(String type){
        for (HTTPType value : HTTPType.values()) {
            if(value.getMethod().equalsIgnoreCase(type)){
                return value;
            }
        }
        return HTTPType.GET;
    }
}
