package dev.soulfur.hydrogen.database;

import dev.soulfur.hydrogen.Hydrogen;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class DatabaseManager {

    private final Hydrogen plugin;
    private Connection connection;

    // Whitelist for allowed columns to be retrieved via the generic field getter.
    private static final Set<String> ALLOWED_COLUMNS = new HashSet<>(Arrays.asList(
            "uuid", "name", "coins", "xp", "level", "rank",
            "language", "first_join", "last_join", "vanished", "flight"
    ));

    public DatabaseManager(Hydrogen plugin) {
        this.plugin = plugin;
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

            // Create tables, including a column for flight.
            createTables();

            plugin.getLogger().info("Database connection established.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database: " + e.getMessage(), e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Create player_data table with additional flight column.
            statement.execute("CREATE TABLE IF NOT EXISTS player_data (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "coins INTEGER DEFAULT 0, " +
                    "xp INTEGER DEFAULT 0, " +
                    "level INTEGER DEFAULT 0, " +
                    "rank TEXT DEFAULT 'default', " +
                    "language TEXT DEFAULT 'en', " +
                    "first_join BIGINT, " +
                    "last_join BIGINT, " +
                    "vanished BOOLEAN DEFAULT 0, " +
                    "flight BOOLEAN DEFAULT 0)");
        }
    }

    /**
     * Loads the player data from the database. If data does not exist, creates a new record.
     * Also updates the player's last join and name.
     *
     * @param player the player whose data is to be loaded or created.
     */
    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM player_data WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                long currentTime = System.currentTimeMillis();
                if (rs.next()) {
                    // If player exists, update their last join and name.
                    try (PreparedStatement updatePs = connection.prepareStatement(
                            "UPDATE player_data SET last_join = ?, name = ? WHERE uuid = ?")) {
                        updatePs.setLong(1, currentTime);
                        updatePs.setString(2, player.getName());
                        updatePs.setString(3, uuid.toString());
                        updatePs.executeUpdate();
                    }
                } else {
                    // If player does not exist, create new record.
                    try (PreparedStatement insertPs = connection.prepareStatement(
                            "INSERT INTO player_data (uuid, name, first_join, last_join, vanished, flight) VALUES (?, ?, ?, ?, ?, ?)")) {
                        insertPs.setString(1, uuid.toString());
                        insertPs.setString(2, player.getName());
                        insertPs.setLong(3, currentTime);
                        insertPs.setLong(4, currentTime);
                        insertPs.setBoolean(5, false);
                        insertPs.setBoolean(6, false);
                        insertPs.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + player.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the player's data from the database.
     *
     * @param uuid the player's UUID
     * @return a PlayerData object or null if not found.
     */
    public PlayerData getPlayerData(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM player_data WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PlayerData data = new PlayerData(uuid);
                    data.setName(rs.getString("name"));
                    data.setCoins(rs.getInt("coins"));
                    data.setXp(rs.getInt("xp"));
                    data.setLevel(rs.getInt("level"));
                    data.setRank(rs.getString("rank"));
                    data.setLanguage(rs.getString("language"));
                    data.setFirstJoin(rs.getLong("first_join"));
                    data.setLastJoin(rs.getLong("last_join"));
                    data.setVanished(rs.getBoolean("vanished"));
                    data.setFlying(rs.getBoolean("flight"));
                    return data;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get player data for " + uuid + ": " + e.getMessage(), e);
        }
        return null;
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    /**
     * Saves the provided PlayerData back to the database.
     *
     * @param data the PlayerData object to be saved.
     */
    public void savePlayerData(PlayerData data) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_data SET " +
                        "name = ?, " +
                        "coins = ?, " +
                        "xp = ?, " +
                        "level = ?, " +
                        "rank = ?, " +
                        "language = ?, " +
                        "last_join = ?, " +
                        "vanished = ?, " +
                        "flight = ? " +
                        "WHERE uuid = ?")) {
            ps.setString(1, data.getName());
            ps.setInt(2, data.getCoins());
            ps.setInt(3, data.getXp());
            ps.setInt(4, data.getLevel());
            ps.setString(5, data.getRank());
            ps.setString(6, data.getLanguage());
            ps.setLong(7, data.getLastJoin());
            ps.setBoolean(8, data.isVanished());
            ps.setBoolean(9, data.isFlying());
            ps.setString(10, data.getUuid().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + data.getUuid() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Saves the player's data by retrieving it first and then updating the record.
     *
     * @param player the player whose data is to be saved.
     */
    public void savePlayerData(Player player) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            // Update the name and last join on save.
            data.setName(player.getName());
            data.setLastJoin(System.currentTimeMillis());
            savePlayerData(data);
        }
    }

    /**
     * Updates a player's vanished status directly in the database.
     *
     * @param uuid     the player's UUID
     * @param vanished the vanish status to set
     */
    public void setVanished(UUID uuid, boolean vanished) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_data SET vanished = ? WHERE uuid = ?")) {
            ps.setBoolean(1, vanished);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update vanish status for " + uuid + ": " + e.getMessage(), e);
        }
    }

    /**
     * Updates a player's flying status directly in the database.
     *
     * @param uuid   the player's UUID
     * @param flight the flight status to set
     */
    public void setFlying(UUID uuid, boolean flight) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_data SET flight = ? WHERE uuid = ?")) {
            ps.setBoolean(1, flight);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update flight status for " + uuid + ": " + e.getMessage(), e);
        }
    }

    /**
     * A generic method to retrieve a specific field from the player_data table.
     * The column name is checked against an allowed list to prevent SQL injection.
     *
     * @param uuid  the player's UUID
     * @param field the column name to retrieve
     * @return an Optional containing the value (as an Object) if present, or an empty Optional if not found or not allowed.
     */
    public Optional<Object> getPlayerField(UUID uuid, String field) {
        if (!ALLOWED_COLUMNS.contains(field)) {
            plugin.getLogger().warning("Field '" + field + "' is not allowed to be queried.");
            return Optional.empty();
        }
        String query = "SELECT " + field + " FROM player_data WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getObject(field));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to fetch field '" + field + "' for " + uuid + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves the player's coins.
     *
     * @param uuid the player's UUID
     * @return the number of coins the player has.
     */
    public int getPlayerCoins(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT coins FROM player_data WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("coins");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to fetch coins for " + uuid + ": " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Retrieves the player's XP.
     *
     * @param uuid the player's UUID
     * @return the player's XP.
     */
    public int getPlayerXp(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT xp FROM player_data WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("xp");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to fetch XP for " + uuid + ": " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Retrieves the player's level.
     *
     * @param uuid the player's UUID
     * @return the player's level.
     */
    public int getPlayerLevel(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT level FROM player_data WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("level");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to fetch level for " + uuid + ": " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Retrieves the player's name.
     *
     * @param uuid the player's UUID
     * @return the player's name, or null if not found.
     */
    public String getPlayerName(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT name FROM player_data WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to fetch name for " + uuid + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Retrieves the player's vanished status.
     *
     * @param uuid the player's UUID
     * @return true if vanished, otherwise false.
     */
    public boolean isPlayerVanished(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT vanished FROM player_data WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("vanished");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to fetch vanished status for " + uuid + ": " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close database connection: " + e.getMessage(), e);
        }
    }
}