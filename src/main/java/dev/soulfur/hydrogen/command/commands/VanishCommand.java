package dev.soulfur.hydrogen.command.commands;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.command.CommandHandler;
import dev.soulfur.hydrogen.database.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class VanishCommand extends CommandHandler {

    private final Map<String, BukkitTask> vanishTasks = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public VanishCommand(Hydrogen plugin) {
        super("vanish", plugin);
        this.setPermission("hydrogen.youtube.vanish");
        this.setDescription("Toggles vanish mode and saves the result to the player database");
        this.setUsage("/vanish");
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        // This command is only for self-use: no arguments allowed.
        if (!(sender instanceof Player)) {
            sendMessage(sender, "command.player-only");
            return true;
        }
        if (args.length > 0) {
            // If any arguments are passed, notify the user of invalid usage.
            sendMessage(sender, "command.invalid-usage");
            return true;
        }
        Player player = (Player) sender;
        toggleVanish(player);
        return true;
    }

    private void toggleVanish(Player player) {
        DatabaseManager db = plugin.getDatabaseManager();
        // Query the current vanish state from the database.
        boolean currentState = db.isPlayerVanished(player.getUniqueId());
        boolean newState = !currentState;
        // Update the vanish state in the database.
        db.setVanished(player.getUniqueId(), newState);
        // Immediately update vanish for all online players.
        vanish(player, newState);

        if (newState) {
            sendMessage(player, "command.vanish.enabled");
            // Schedule a task to display the vanish action bar message periodically.
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    // Check if the player is still online and has vanish enabled (queried from the database).
                    if (!player.isOnline() || !plugin.getDatabaseManager().isPlayerVanished(player.getUniqueId())) {
                        cancel();
                        vanishTasks.remove(player.getUniqueId().toString());
                        return;
                    }
                    player.sendActionBar(parseMiniMessage(player));
                }
            }.runTaskTimer(plugin, 0L, 20L);
            vanishTasks.put(player.getUniqueId().toString(), task);
        } else {
            sendMessage(player, "command.vanish.disabled");
            BukkitTask task = vanishTasks.remove(player.getUniqueId().toString());
            if (task != null) {
                task.cancel();
            }
        }
    }

    private void vanish(Player player, boolean vanish) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (vanish) {
                online.hidePlayer(plugin, player);
            } else {
                online.showPlayer(plugin, player);
            }
        }
    }

    /**
     * Retrieves the vanish action bar message from the language configuration,
     * allowing placeholder support if needed, and converts it to a Component.
     *
     * @param player The player (for placeholder context, if needed).
     * @return The deserialized Component.
     */
    private Component parseMiniMessage(Player player) {
        String raw = plugin.getConfigManager().getMessage("command.vanish.actionbar");
        // Placeholder support could be added here using a Map if required.
        return miniMessage.deserialize(raw);
    }
}
