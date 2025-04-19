package dev.soulfur.hydrogen.modules.chat;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.config.ConfigManager;
import dev.soulfur.hydrogen.modules.rank.RankModule;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatModule implements Listener {

    private final Hydrogen plugin;
    private final ConfigManager configManager;
    private final RankModule rankModule;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatModule(Hydrogen plugin, ConfigManager configManager, RankModule rankModule) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.rankModule = rankModule;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        // Cancel the default chat handling.
        event.setCancelled(true);

        // Retrieve player information.
        String playerName = event.getPlayer().getName();

        // Convert the Component chat message to plain text.
        String originalMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        // Sanitize the player's message to disable MiniMessage formatting.
        // This replaces '<' and '>' so any attempt to inject MiniMessage tags is neutralized.
        String safeMessage = originalMessage.replace("<", "&lt;").replace(">", "&gt;");

        // Retrieve the rank prefix from the RankModule.
        String rankPrefix = rankModule.getRankPrefix(event.getPlayer());

        // Fetch the chat format from your language or config file.
        // Example entry in your messages file:
        //   chat.format: "<rank> <player>: <message>"
        String chatFormat = configManager.getMessage("chat.format");

        // Replace placeholders with actual (sanitized) content.
        chatFormat = chatFormat.replace("<rank>", rankPrefix)
                .replace("<player>", playerName)
                .replace("<message>", safeMessage);

        // Deserialize the final formatted string using MiniMessage.
        Component formattedMessage = miniMessage.deserialize(chatFormat);

        // Broadcast the formatted message to all online players.
        plugin.getServer().getOnlinePlayers().forEach(player ->
                player.sendMessage(formattedMessage)
        );
    }
}
