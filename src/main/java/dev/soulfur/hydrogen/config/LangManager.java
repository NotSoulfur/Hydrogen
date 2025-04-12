package dev.soulfur.hydrogen.config;

import dev.soulfur.hydrogen.Hydrogen;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LangManager {

    private final Hydrogen plugin;
    private final Map<String, FileConfiguration> languages;
    private final String defaultLanguage = "en";
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Corrected constructor with the class name matching
    public LangManager(Hydrogen plugin) {
        this.plugin = plugin;
        this.languages = new HashMap<>();
    }

    public void loadLanguages() {
        // Create language directory if it doesn't exist
        File langDir = new File(plugin.getDataFolder(), "languages");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        // Create default language files
        createDefaultLanguage("messages_en.yml");

        // Load all available language files
        for (File file : langDir.listFiles()) {
            if (file.isFile() && file.getName().startsWith("messages_") && file.getName().endsWith(".yml")) {
                String langCode = file.getName().substring(9, file.getName().length() - 4);
                loadLanguage(langCode);
            }
        }
    }

    private void createDefaultLanguage(String fileName) {
        File file = new File(plugin.getDataFolder() + "/languages", fileName);
        if (!file.exists()) {
            plugin.saveResource("languages/" + fileName, false);
        }
    }

    private void loadLanguage(String langCode) {
        File file = new File(plugin.getDataFolder() + "/languages", "messages_" + langCode + ".yml");
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            languages.put(langCode, config);
            plugin.getLogger().info("Loaded language: " + langCode);
        }
    }

    public String getLanguage(Player player) {
        // Get player's language from database or config
        // For now, return default language
        return defaultLanguage;
    }

    public String getMessage(String key) {
        return getMessage(defaultLanguage, key);
    }

    public String getMessage(String langCode, String key) {
        FileConfiguration lang = languages.getOrDefault(langCode, languages.get(defaultLanguage));
        if (lang == null) {
            return "Language not found: " + langCode;
        }
        return lang.getString(key, "Message key not found: " + key);
    }

    public void sendMessage(Player player, String key) {
        String message = getMessage(getLanguage(player), key);
        player.sendMessage(miniMessage.deserialize(message));
    }

    public void sendMessage(Player player, String key, Map<String, String> placeholders) {
        String message = getMessage(getLanguage(player), key);

        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        player.sendMessage(miniMessage.deserialize(message));
    }

    public Component getComponent(String key) {
        String message = getMessage(key);
        return miniMessage.deserialize(message);
    }

    public Component getComponent(String langCode, String key) {
        String message = getMessage(langCode, key);
        return miniMessage.deserialize(message);
    }
}
