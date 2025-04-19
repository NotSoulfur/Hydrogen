package dev.soulfur.hydrogen.modules.rank;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.database.DatabaseManager;
import dev.soulfur.hydrogen.database.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class RankModule {

    private final Hydrogen plugin;
    private final DatabaseManager databaseManager;
    // The ranks config (for defined prefixes, etc.) loaded by your ConfigManager.
    private final FileConfiguration ranksConfig;

    public RankModule(Hydrogen plugin) {
        this.plugin = plugin;
        // Assumes DatabaseManager is already initialized on plugin startup.
        this.databaseManager = plugin.getDatabaseManager();
        // Retrieve the ranks configuration from your ConfigManager.
        this.ranksConfig = plugin.getConfigManager().getConfig("ranks.yml");
        if (ranksConfig != null) {
            plugin.getLogger().info("RankModule: Ranks configuration loaded successfully.");
        } else {
            plugin.getLogger().warning("RankModule: Ranks configuration not found!");
        }
    }

    /**
     * Retrieves the rank prefix for the given player by:
     * 1. Retrieving the player's data from the database.
     * 2. Extracting the rank identifier.
     * 3. Looking up the prefix in the configuration.
     * Returns a default prefix if any step fails.
     */
    public String getRankPrefix(Player player) {
        PlayerData data = databaseManager.getPlayerData(player);
        String rank = data != null ? data.getRank() : "default";

        // Look up the prefix from the ranks configuration.
        // Expected config structure:
        // ranks:
        //   default:
        //     prefix: "&7[Member]"
        //   admin:
        //     prefix: "&c[Admin]"
        String prefix = ranksConfig != null ? ranksConfig.getString("ranks." + rank + ".prefix") : null;
        if (prefix == null) {
            // Fallback to a default prefix if not found.
            prefix = "&7[Member]";
        }
        return prefix;
    }

    /**
     * Updates the player's rank both in the in-memory PlayerData and persists it to the database.
     *
     * @param player The player whose rank will be updated.
     * @param newRank The new rank identifier.
     */
    public void setPlayerRank(Player player, String newRank) {
        PlayerData data = databaseManager.getPlayerData(player);
        if (data != null) {
            data.setRank(newRank);
            databaseManager.savePlayerData(data);
            plugin.getLogger().info("Updated rank for " + player.getName() + " to " + newRank);
        } else {
            plugin.getLogger().warning("Could not update rank: Data for " + player.getName() + " not found.");
        }
    }

    /**
     * (Optional) Reloads the rank configuration if needed.
     */
    public void reloadRanksConfig() {
        plugin.getConfigManager().reloadConfig("ranks.yml");
    }
}
