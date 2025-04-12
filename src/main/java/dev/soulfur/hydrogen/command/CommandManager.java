package dev.soulfur.hydrogen.command;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.command.commands.*;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final Hydrogen plugin;
    private final List<CommandHandler> commands;

    public CommandManager(Hydrogen plugin) {
        this.plugin = plugin;
        this.commands = new ArrayList<>();
    }

    public void registerCommands() {
        // Register commands
        registerCommand(new GamemodeCommand(plugin));
        registerCommand(new FlyCommand(plugin));
        registerCommand(new SetLobbyCommand(plugin));
        registerCommand(new LobbyCommand(plugin));
        registerCommand(new VanishCommand(plugin));

        plugin.getLogger().info("Registered " + commands.size() + " commands");

        // Process disabled commands
        processDisabledCommands();
    }

    private void registerCommand(CommandHandler command) {
        try {
            final Field bukkitCommandMap = plugin.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);

            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(plugin.getServer());
            commandMap.register(plugin.getName().toLowerCase(), command);

            commands.add(command);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register command " + command.getName() + ": " + e.getMessage());
        }
    }

    private void processDisabledCommands() {
        FileConfiguration config = plugin.getConfigManager().getConfig("config.yml");
        if (config == null) return;

        List<String> disabledCommands = config.getStringList("disabled-commands");
        if (disabledCommands == null || disabledCommands.isEmpty()) return;

        for (CommandHandler command : commands) {
            if (disabledCommands.contains(command.getName())) {
                command.setEnabled(false);
            }
        }
    }

    public List<CommandHandler> getCommands() {
        return new ArrayList<>(commands);
    }

    public CommandHandler getCommand(String name) {
        for (CommandHandler command : commands) {
            if (command.getName().equalsIgnoreCase(name)) {
                return command;
            }

            for (String alias : command.getAliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return command;
                }
            }
        }

        return null;
    }
}