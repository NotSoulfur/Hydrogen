package dev.soulfur.hydrogen.database;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String name;
    private int coins;
    private int xp;
    private int level;
    private String rank;
    private String language;
    private long firstJoin;
    private long lastJoin;
    private boolean vanished;
    private boolean flight;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.coins = 0;
        this.xp = 0;
        this.level = 1;
        this.rank = "default";
        this.language = "en";
        this.vanished = false;
        this.flight = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void addCoins(int amount) {
        this.coins += amount;
    }

    public boolean removeCoins(int amount) {
        if (coins >= amount) {
            coins -= amount;
            return true;
        }
        return false;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
        updateLevel();
    }

    public void addXp(int amount) {
        this.xp += amount;
        updateLevel();
    }

    private void updateLevel() {
        this.level = 1 + (int) Math.sqrt(xp / 100);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getFirstJoin() {
        return firstJoin;
    }

    public void setFirstJoin(long firstJoin) {
        this.firstJoin = firstJoin;
    }

    public long getLastJoin() {
        return lastJoin;
    }

    public void setLastJoin(long lastJoin) {
        this.lastJoin = lastJoin;
    }

    public boolean isVanished() {
        return vanished;
    }

    public void setVanished(boolean vanished) {
        this.vanished = vanished;
    }

    public boolean isFlying() {
        return flight;
    }

    public void setFlying(boolean flight) {
        this.flight = flight;
    }
}
