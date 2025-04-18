package dev.soulfur.hydrogen.listeners;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.database.DatabaseManager;
import dev.soulfur.hydrogen.database.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final Hydrogen plugin;

    public PlayerJoin(Hydrogen plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        DatabaseManager db = plugin.getDatabaseManager();

        // Initialize player data; creates a new record in the database if one doesn't exist.
        db.loadPlayerData(player);

        // Retrieve the player's data from the database.
        PlayerData data = db.getPlayerData(player);
        if (data == null) {
            return;
        }

        boolean vanished = data.isVanished();
        boolean flying = data.isFlying();

        // Restore flight status: allow flight and toggle flying state if flagged.
        if (flying) {
            player.setAllowFlight(true);
            player.setFlying(true);
        } else {
            player.setAllowFlight(false);
            player.setFlying(false);
        }

        // Restore vanish status: hide the player if vanished, otherwise ensure visibility.
        if (vanished) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    online.hidePlayer(plugin, player);
                }
            }
        } else {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, player);
            }
        }
    }
}
