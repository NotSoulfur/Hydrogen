package dev.soulfur.hydrogen.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class Actionbar {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * Sends an action bar message to the specified player.
     *
     * @param player  the player to send the action bar to
     * @param message the message string in MiniMessage format
     */
    public static void sendActionBar(Player player, String message) {
        // Deserialize the MiniMessage formatted string into an Adventure Component.
        Component actionBarComponent = MINI_MESSAGE.deserialize(message);

        // Send the ActionBar component to the player.
        player.sendActionBar(actionBarComponent);
    }
}
