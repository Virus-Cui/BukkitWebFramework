package cn.mrcsh.bukkitwebframework.Bukkit.Utils;

import org.bukkit.Bukkit;

public class LogUtils {
    public static boolean noLog = false;

    public static void info(String msg) {
        if (!noLog) {
            Bukkit.getConsoleSender().sendMessage("§c[§eBukkitWeb§c]§f:" + msg.replaceAll("&", "§"));
        }
    }

    public static void info(String msg, Object... objs) {
        if (!noLog) {
            Bukkit.getConsoleSender().sendMessage("§c[§eBukkitWeb§c]§f:" + String.format(msg.replaceAll("&", "§"), objs));
        }
    }

    public static void error(String msg, Exception e) {
        if (!noLog) {
            Bukkit.getConsoleSender().sendMessage("§c[§eBukkitWeb§c]§f:" + "§c" + msg + e.getMessage());
        }
    }

}
