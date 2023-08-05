package cn.mrcsh.bukkitwebframework.Utils;

import org.bukkit.Bukkit;

public class LogUtils {
    public static void info(String msg){
        Bukkit.getConsoleSender().sendMessage(msg.replaceAll("&","§"));
    }

    public static void info(String msg, Object ... objs){
        Bukkit.getConsoleSender().sendMessage(String.format(msg.replaceAll("&","§"),objs));
    }

    public static void error(String msg, Exception e){
        Bukkit.getConsoleSender().sendMessage("§c"+msg+e.getMessage());
    }

}
