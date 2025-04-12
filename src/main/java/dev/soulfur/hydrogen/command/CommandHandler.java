package dev.soulfur.hydrogen.command;

import dev.soulfur.hydrogen.Hydrogen;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class CommandHandler extends Command {

    protected final Hydrogen plugin;
    protected final MiniMessage miniMessage = MiniMessage.miniMessage();
    private boolean enabled = true;

    public CommandHandler(@NotNull String name, Hydrogen plugin) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
        if (!enabled) {
            sender.sendMessage(plugin.getLangManager().getComponent("command.disabled"));
            return true;
        }

        if (!hasPermission(sender)) {
            sender.sendMessage(plugin.getLangManager().getComponent("command.no-permission"));
            return true;
        }

        if (requiresPlayer() && !(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangManager().getComponent("command.player-only"));
            return true;
        }

        return execute(sender, args);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
        if (!enabled || !hasPermission(sender)) {
            return new ArrayList<>();
        }

        if (requiresPlayer() && !(sender instanceof Player)) {
            return new ArrayList<>();
        }

        return onTabComplete(sender, args);
    }

    /**
     * Check if the sender has permission to use this command
     *
     * @param sender The command sender
     * @return True if the sender has permission
     */
    protected boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    /**
     * Check if this command requires a player
     *
     * @return True if this command requires a player
     */
    protected boolean requiresPlayer() {
        return false;
    }

    /**
     * Execute the command
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return True if the command was successful
     */
    protected abstract boolean execute(CommandSender sender, String[] args);

    /**
     * Handle tab completion
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return A list of tab completions
     */
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    /**
     * Send a language message to the command sender
     *
     * @param sender The command sender
     * @param key    The language key
     */
    protected void sendMessage(CommandSender sender, String key) {
        Component message = plugin.getLangManager().getComponent(key);
        sender.sendMessage(message);
    }

    /**
     * Send a language message with placeholders to the command sender
     *
     * @param sender       The command sender
     * @param key          The language key
     * @param placeholders The placeholders
     */
    protected void sendMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        String message = plugin.getLangManager().getMessage(key);

        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        sender.sendMessage(miniMessage.deserialize(message));
    }

    /**
     * Check if this command is enabled
     *
     * @return True if this command is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set if this command is enabled
     *
     * @param enabled True if this command should be enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}