package dev.soulfur.hydrogen.modules.vanish;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.database.DatabaseManager;
import org.bukkit.entity.Player;

public class VanishManager {

    private final Hydrogen plugin;
    private final DatabaseManager databaseManager;

    public VanishManager(Hydrogen plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    public boolean toggleVanish(Player player) {
        boolean isVanished = isVanished(player);

        // Toggle vanish state
        if (isVanished) {
            setVanished(player, false);
        } else {
            setVanished(player, true);
        }

        return !isVanished;
    }

    public boolean isVanished(Player player) {
        return databaseManager.isPlayerVanished(player.getUniqueId());
    }

    public void setVanished(Player player, boolean vanished) {
        databaseManager.setVanished(player.getUniqueId(), vanished);

        // Hide or show the player
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (vanished) {
                online.hidePlayer(plugin, player);
            } else {
                online.showPlayer(plugin, player);
            }
        }
    }
}
