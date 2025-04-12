package dev.soulfur.hydrogen.config;

import dev.soulfur.hydrogen.Hydrogen;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final Hydrogen plugin;
    private final Map<String, FileConfiguration> configs;

    public ConfigManager(Hydrogen plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
    }

    public void loadConfigs() {
        // Create default configs
        createDefaultConfig("config.yml");
        createDefaultConfig("holograms.yml");
        createDefaultConfig("npcs.yml");
        createDefaultConfig("launchpads.yml");
        createDefaultConfig("ranks.yml");

        // Load all configs
        loadConfig("config.yml");
        loadConfig("holograms.yml");
        loadConfig("npcs.yml");
        loadConfig("launchpads.yml");
        loadConfig("ranks.yml");
    }

    private void createDefaultConfig(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
    }

    private void loadConfig(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            configs.put(name, config);
            plugin.getLogger().info("Loaded configuration file: " + name);
        } else {
            plugin.getLogger().warning("Configuration file not found: " + name);
        }
    }

    public FileConfiguration getConfig(String name) {
        return configs.getOrDefault(name, null);
    }

    public void saveConfig(String name) {
        if (configs.containsKey(name)) {
            try {
                configs.get(name).save(new File(plugin.getDataFolder(), name));
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save config " + name + ": " + e.getMessage());
            }
        }
    }

    public void reloadConfig(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            configs.put(name, config);
        }
    }

    public void reloadAllConfigs() {
        for (String configName : configs.keySet()) {
            reloadConfig(configName);
        }
    }
}