package cn.mrcsh.bukkitwebframework.Enum;

public enum Mode {
    WEB("web","单网页模式"),
    BACKEND("backend", "单后端模式"),
    MIXED("mixed","混合模式")
    ;

    private String mode;
    private String desc;

    Mode(String mode, String desc) {
        this.mode = mode;
        this.desc = desc;
    }

    public String getMode() {
        return mode;
    }


    public String getDesc() {
        return desc;
    }

    public static Mode findMode(String mode){
        for (Mode value : Mode.values()) {
            if(value.mode.equalsIgnoreCase(mode)){
                return value;
            }
        }
        return Mode.MIXED;
    }
}
