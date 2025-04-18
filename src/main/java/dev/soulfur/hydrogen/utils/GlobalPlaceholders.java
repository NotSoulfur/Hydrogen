package dev.soulfur.hydrogen.utils;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Map;

public class GlobalPlaceholders {

    /**
     * Formats an input string by replacing placeholders with provided values.
     * Placeholders should be in the format: %key%
     *
     * @param input        The input string containing placeholders.
     * @param replacements A map containing keys and their respective replacement values.
     * @return The formatted string.
     */
    public static String format(String input, Map<String, String> replacements) {
        if (input == null || replacements == null) {
            return input;
        }
        String result = input;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return result;
    }

    /**
     * Returns a nicely formatted representation of the player's gamemode.
     *
     * @param player The target player.
     * @return A string representation of the gamemode (e.g., "Survival").
     */
    public static String getGamemodePlaceholder(Player player) {
        GameMode mode = player.getGameMode();
        return mode.name().charAt(0) + mode.name().substring(1).toUpperCase();
    }

    // Future placeholder methods can be added here.
}
