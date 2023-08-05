package cn.mrcsh.bukkitwebframework.Utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigUtils {

    public static JavaPlugin javaPlugin;

    public static void save(String fileName, boolean replace) {
        File dataFolder = javaPlugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        javaPlugin.saveResource(fileName, replace);
    }

    public static YamlConfiguration loadConfig(String configName) {
        if (configName.startsWith("/")) {
            configName = configName.replaceFirst("/", "");
        }
        File configFile = new File(javaPlugin.getDataFolder(), configName);
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(configFile);

        } catch (IOException | InvalidConfigurationException e) {
            LogUtils.error("加载配置文件发生错误", e);
        }
        LogUtils.info("&6加载%s成功", configName);
        return configuration;
    }
}
