package dev.soulfur.hydrogen.config;

import dev.soulfur.hydrogen.Hydrogen;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final Hydrogen plugin;

    // Map for standalone config files (e.g., config.yml, holograms.yml, npcs.yml, launchpads.yml, ranks.yml)
    private final Map<String, FileConfiguration> configs;
    // Map for language files (located in the "languages" folder)
    private final Map<String, FileConfiguration> languages;
    // Map for menu files (located in the "menus" folder)
    private final Map<String, FileConfiguration> menus;

    private final String defaultLanguage = "en";
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ConfigManager(Hydrogen plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.languages = new HashMap<>();
        this.menus = new HashMap<>();
    }

    /*===============================
      GENERAL CONFIGURATION METHODS
    ===============================*/

    public void loadConfigs() {
        // Create and load default standalone config files.
        createDefaultConfig("config.yml");
        createDefaultConfig("holograms.yml");
        createDefaultConfig("npcs.yml");
        createDefaultConfig("launchpads.yml");
        createDefaultConfig("ranks.yml");

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
            plugin.getLogger().info("Reloaded configuration file: " + name);
        }
    }

    public void reloadAllConfigs() {
        for (String configName : configs.keySet()) {
            reloadConfig(configName);
        }
    }

    /*===========================
      LANGUAGE CONFIGURATION METHODS
    ===========================*/

    public void loadLanguages() {
        File langDir = new File(plugin.getDataFolder(), "languages");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        // Create the default language file if necessary.
        createDefaultLanguage("messages_en.yml");

        // Load all language files, assuming they are named in the format: messages_<langCode>.yml
        File[] files = langDir.listFiles(file -> file.isFile() &&
                file.getName().startsWith("messages_") &&
                file.getName().endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String langCode = fileName.substring(9, fileName.length() - 4);
                loadLanguage(langCode);
            }
        }
    }

    private void createDefaultLanguage(String fileName) {
        File file = new File(plugin.getDataFolder() + File.separator + "languages", fileName);
        if (!file.exists()) {
            plugin.saveResource("languages/" + fileName, false);
        }
    }

    private void loadLanguage(String langCode) {
        File file = new File(plugin.getDataFolder() + File.separator + "languages", "messages_" + langCode + ".yml");
        if (file.exists()) {
            FileConfiguration langConfig = YamlConfiguration.loadConfiguration(file);
            languages.put(langCode, langConfig);
            plugin.getLogger().info("Loaded language: " + langCode);
        }
    }

    /**
     * Retrieves a message from the default language configuration.
     *
     * @param key The message key.
     * @return The message string.
     */
    public String getMessage(String key) {
        FileConfiguration lang = languages.getOrDefault(defaultLanguage, null);
        if (lang == null) {
            return "Language file not found: " + defaultLanguage;
        }
        return lang.getString(key, "Message key not found: " + key);
    }

    /**
     * Retrieves a Component for a message from the default language.
     *
     * @param key The message key.
     * @return The Component.
     */
    public Component getComponent(String key) {
        String message = getMessage(key);
        return miniMessage.deserialize(message);
    }

    /*===========================
      MENU CONFIGURATION METHODS
    ===========================*/

    public void loadMenus() {
        File menusDir = new File(plugin.getDataFolder(), "menus");
        if (!menusDir.exists()) {
            menusDir.mkdirs();
        }

        createDefaultMenu("games.yml");

        File[] files = menusDir.listFiles(file -> file.isFile() &&
                file.getName().toLowerCase().endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String key = fileName.substring(0, fileName.lastIndexOf('.'));
                loadMenu(key);
            }
        }
    }

    private void createDefaultMenu(String fileName) {
        File file = new File(plugin.getDataFolder() + File.separator + "menus", fileName);
        if (!file.exists()) {
            plugin.saveResource("menus/" + fileName, false);
        }
    }

    private void loadMenu(String key) {
        File file = new File(plugin.getDataFolder() + File.separator + "menus", key + ".yml");
        if (file.exists()) {
            FileConfiguration menuConfig = YamlConfiguration.loadConfiguration(file);
            menus.put(key, menuConfig);
            plugin.getLogger().info("Loaded menu configuration: " + key);
        }
    }

    public FileConfiguration getMenu(String key) {
        return menus.getOrDefault(key, null);
    }

    /*====================
      GENERAL GETTERS
    ====================*/

    public Map<String, FileConfiguration> getAllLanguages() {
        return languages;
    }

    public Map<String, FileConfiguration> getAllConfigs() {
        return configs;
    }

    public Map<String, FileConfiguration> getAllMenus() {
        return menus;
    }
}
