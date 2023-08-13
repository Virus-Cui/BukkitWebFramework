package cn.mrcsh.bukkitwebframework.Bungee.Utils;


import net.md_5.bungee.api.plugin.Plugin;

public class LogUtils {
    public static boolean noLog = false;

    public static Plugin plugin;

    public static void info(String msg) {
        if (!noLog) {
            plugin.getProxy().getConsole().sendMessage("§c[§eBungeeWeb§c]§f:" + msg.replaceAll("&", "§"));
        }
    }

    public static void info(String msg, Object... objs) {
        if (!noLog) {
            plugin.getProxy().getConsole().sendMessage("§c[§eBungeeWeb§c]§f:" + String.format(msg.replaceAll("&", "§"), objs));
        }
    }

    public static void error(String msg, Exception e) {
        if (!noLog) {
            plugin.getProxy().getConsole().sendMessage("§c[§eBungeeWeb§c]§f:" + "§c" + msg + e.getMessage());
        }
    }

}
