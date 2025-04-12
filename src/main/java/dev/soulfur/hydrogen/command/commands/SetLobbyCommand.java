package dev.soulfur.hydrogen.command.commands;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.command.CommandHandler;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand extends CommandHandler {

    public SetLobbyCommand(Hydrogen plugin) {
        super("setlobby", plugin);
        this.setPermission("hydrogen.command.setlobby");
        this.setDescription("Sets the lobby location and saves it to config.yml");
        this.setUsage("/setlobby");
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "command.player-only");
            return true;
        }
        Player player = (Player) sender;
        Location loc = player.getLocation();

        // Save lobby location to the configuration
        plugin.getConfig().set("lobby.world", loc.getWorld().getName());
        plugin.getConfig().set("lobby.x", loc.getX());
        plugin.getConfig().set("lobby.y", loc.getY());
        plugin.getConfig().set("lobby.z", loc.getZ());
        plugin.getConfig().set("lobby.yaw", loc.getYaw());
        plugin.getConfig().set("lobby.pitch", loc.getPitch());
        plugin.saveConfig(); // Persist the changes

        sendMessage(player, "command.setlobby.success");
        return true;
    }
}
