package dev.soulfur.hydrogen.command.commands;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.command.CommandHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VanishCommand extends CommandHandler {

    // Tracks vanished players in-memory
    private final Set<Player> vanished = new HashSet<>();
    // Maps player UUIDs to their scheduled actionbar display tasks
    private final Map<String, BukkitTask> vanishTasks = new HashMap<>();

    // Instance of MiniMessage to create the actionbar component
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public VanishCommand(Hydrogen plugin) {
        super("vanish", plugin);
        this.setPermission("hydrogen.command.vanish");
        this.setDescription("Toggles vanish mode and saves the result to the player database");
        this.setUsage("/vanish [player]");
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            // Toggle vanish for another player
            if (!sender.hasPermission("hydrogen.command.vanish.others")) {
                sendMessage(sender, "command.no-permission");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendMessage(sender, "command.player-not-found");
                return true;
            }
            toggleVanish(sender, target);
        } else {
            // Toggle vanish for the executing player
            if (!(sender instanceof Player)) {
                sendMessage(sender, "command.player-only");
                return true;
            }
            Player player = (Player) sender;
            toggleVanish(sender, player);
        }
        return true;
    }

    private void toggleVanish(CommandSender sender, Player player) {
        boolean isVanished = vanished.contains(player);
        if (isVanished) {
            // Turn vanish off
            vanish(player, false);
            vanished.remove(player);
            // Cancel the actionbar repeating task if one exists
            BukkitTask task = vanishTasks.remove(player.getUniqueId().toString());
            if (task != null) {
                task.cancel();
            }
            // Persist the vanish state change to the player database
            plugin.getDatabaseManager().setVanished(player.getUniqueId(), false);
            sendMessage(player, "command.vanish.disabled");
            if (sender != player) {
                sendMessage(sender, "command.vanish.disabled-other", Map.of("player", player.getName()));
            }
        } else {
            // Turn vanish on
            vanish(player, true);
            vanished.add(player);
            // Persist the vanish state change to the player database
            plugin.getDatabaseManager().setVanished(player.getUniqueId(), true);
            sendMessage(player, "command.vanish.enabled");
            if (sender != player) {
                sendMessage(sender, "command.vanish.enabled-other", Map.of("player", player.getName()));
            }
            // Schedule a repeating task to display an actionbar message
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    // If the player disconnects or is no longer vanished, cancel this task
                    if (!player.isOnline() || !vanished.contains(player)) {
                        cancel();
                        vanishTasks.remove(player.getUniqueId().toString());
                        return;
                    }
                    // Send actionbar message using MiniMessage formatting
                    // Adjust the interval or message as needed
                    player.sendActionBar(miniMessage.deserialize("<aqua>You are currently </aqua><yellow>VANISHED</yellow>"));
                }
            }.runTaskTimer(plugin, 0L, 20L); // Runs every second (20 ticks)
            vanishTasks.put(player.getUniqueId().toString(), task);
        }
    }

    /**
     * Handles the actual toggling visibility, hiding or showing the player to all online players.
     *
     * @param player the player whose vanish state is changing
     * @param vanish if true, hides the player from others; if false, shows the player
     */
    private void vanish(Player player, boolean vanish) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (vanish) {
                online.hidePlayer(plugin, player);
            } else {
                online.showPlayer(plugin, player);
            }
        }
    }
}
