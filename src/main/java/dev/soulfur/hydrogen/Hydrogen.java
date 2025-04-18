package dev.soulfur.hydrogen;

import dev.soulfur.hydrogen.command.CommandManager;
import dev.soulfur.hydrogen.config.ConfigManager;
import dev.soulfur.hydrogen.database.DatabaseManager;
import dev.soulfur.hydrogen.listeners.HungerListener;
import dev.soulfur.hydrogen.listeners.PlayerJoin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Hydrogen extends JavaPlugin {

    private static Hydrogen instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Loading Managers...");

        // Initialize the combined configuration manager, which loads general configs, languages, and menus.
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();
        this.configManager.loadLanguages();
        this.configManager.loadMenus();

        // Initialize the database manager.
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initialize();

        // Initialize the command manager.
        this.commandManager = new CommandManager(this);
        this.commandManager.registerCommands();

        getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
        getServer().getPluginManager().registerEvents(new HungerListener(), this);



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

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public FileConfiguration getLangConfig() {
        return getConfigManager().getAllLanguages().get("en");
    }

}
