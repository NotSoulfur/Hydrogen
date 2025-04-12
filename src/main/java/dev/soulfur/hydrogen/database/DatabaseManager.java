package dev.soulfur.hydrogen.database;

import dev.soulfur.hydrogen.Hydrogen;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {

    private final Hydrogen plugin;
    private Connection connection;
    private final Map<UUID, PlayerData> playerDataCache;

    public DatabaseManager(Hydrogen plugin) {
        this.plugin = plugin;
        this.playerDataCache = new HashMap<>();
    }

    public void initialize() {
        try {
            // Create database directory
            File databaseDir = new File(plugin.getDataFolder(), "database");
            if (!databaseDir.exists()) {
                databaseDir.mkdirs();
            }

            // Connect to SQLite database
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/database/hydrogen.db");

            // Create tables
            createTables();

            plugin.getLogger().info("Database connection established.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Create player_data table with a vanished column.
            // Note: If the table already exists without the vanished column, you might need to migrate your database.
            statement.execute("CREATE TABLE IF NOT EXISTS player_data (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "coins INTEGER DEFAULT 0, " +
                    "xp INTEGER DEFAULT 0, " +
                    "level INTEGER DEFAULT 1, " +
                    "rank TEXT DEFAULT 'default', " +
                    "language TEXT DEFAULT 'en', " +
                    "first_join BIGINT, " +
                    "last_join BIGINT, " +
                    "vanished BOOLEAN DEFAULT 0)");
        }
    }

    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM player_data WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Player exists, load data
                    PlayerData data = new PlayerData(uuid);
                    data.setName(rs.getString("name"));
                    data.setCoins(rs.getInt("coins"));
                    data.setXp(rs.getInt("xp"));
                    data.setLevel(rs.getInt("level"));
                    data.setRank(rs.getString("rank"));
                    data.setLanguage(rs.getString("language"));
                    data.setFirstJoin(rs.getLong("first_join"));
                    data.setLastJoin(System.currentTimeMillis());
                    data.setVanished(rs.getBoolean("vanished")); // load vanished status

                    // Update last join and name
                    try (PreparedStatement updatePs = connection.prepareStatement(
                            "UPDATE player_data SET last_join = ?, name = ? WHERE uuid = ?")) {
                        updatePs.setLong(1, data.getLastJoin());
                        updatePs.setString(2, player.getName());
                        updatePs.setString(3, uuid.toString());
                        updatePs.executeUpdate();
                    }

                    // Add to cache
                    playerDataCache.put(uuid, data);
                } else {
                    // Player doesn't exist, create new data
                    PlayerData data = new PlayerData(uuid);
                    data.setName(player.getName());
                    data.setFirstJoin(System.currentTimeMillis());
                    data.setLastJoin(System.currentTimeMillis());
                    data.setVanished(false); // default to not vanished

                    // Insert into database
                    try (PreparedStatement insertPs = connection.prepareStatement(
                            "INSERT INTO player_data (uuid, name, first_join, last_join, vanished) VALUES (?, ?, ?, ?, ?)")) {
                        insertPs.setString(1, uuid.toString());
                        insertPs.setString(2, player.getName());
                        insertPs.setLong(3, data.getFirstJoin());
                        insertPs.setLong(4, data.getLastJoin());
                        insertPs.setBoolean(5, data.isVanished());
                        insertPs.executeUpdate();
                    }

                    // Add to cache
                    playerDataCache.put(uuid, data);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load player data for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data == null) return;

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_data SET " +
                        "name = ?, " +
                        "coins = ?, " +
                        "xp = ?, " +
                        "level = ?, " +
                        "rank = ?, " +
                        "language = ?, " +
                        "last_join = ?, " +
                        "vanished = ? " + // update vanished status
                        "WHERE uuid = ?")) {
            ps.setString(1, data.getName());
            ps.setInt(2, data.getCoins());
            ps.setInt(3, data.getXp());
            ps.setInt(4, data.getLevel());
            ps.setString(5, data.getRank());
            ps.setString(6, data.getLanguage());
            ps.setLong(7, data.getLastJoin());
            ps.setBoolean(8, data.isVanished());
            ps.setString(9, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save player data for " + uuid + ": " + e.getMessage());
        }
    }

    public void savePlayerData(Player player) {
        savePlayerData(player.getUniqueId());
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.getOrDefault(uuid, null);
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public void removePlayerData(UUID uuid) {
        playerDataCache.remove(uuid);
    }

    /**
     * Updates a player's vanished status in the database.
     *
     * @param uuid     the player's UUID
     * @param vanished the vanish status to set
     */
    public void setVanished(UUID uuid, boolean vanished) {
        // Update the vanished status in the database
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_data SET vanished = ? WHERE uuid = ?")) {
            ps.setBoolean(1, vanished);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update vanish status for " + uuid + ": " + e.getMessage());
        }

        // Also update the in-memory cache if present
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            data.setVanished(vanished);
        }
    }

    public void close() {
        // Save all cached player data
        for (UUID uuid : playerDataCache.keySet()) {
            savePlayerData(uuid);
        }

        // Clear cache
        playerDataCache.clear();

        // Close connection
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        }
    }
}
