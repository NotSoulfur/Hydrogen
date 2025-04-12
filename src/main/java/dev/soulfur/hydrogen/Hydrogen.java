package dev.soulfur.hydrogen;

import dev.soulfur.hydrogen.command.CommandManager;
import dev.soulfur.hydrogen.config.ConfigManager;
import dev.soulfur.hydrogen.config.LangManager;
import dev.soulfur.hydrogen.database.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Hydrogen extends JavaPlugin {

    private static Hydrogen instance;
    private ConfigManager configManager;
    private LangManager langManager;
    private DatabaseManager databaseManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {

        instance = this;

        getLogger().info("Loading Managers...");

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();

        this.langManager = new LangManager(this);
        this.langManager.loadLanguages();

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initialize();

        this.commandManager = new CommandManager(this);
        this.commandManager.registerCommands();

        getLogger().info("Hydrogen has been enabled!");


    }

    @Override
    public void onDisable() {

        if (this.databaseManager != null) {
            this.databaseManager.close();
        }

        getLogger().info("Hydrogen has been disabled!");

    }

    public static Hydrogen getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
