package dev.soulfur.hydrogen.command.commands;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.command.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LobbyCommand extends CommandHandler {

    public LobbyCommand(Hydrogen plugin) {
        super("lobby", plugin);
        this.setPermission("hydrogen.default.lobby");
        this.setDescription("Teleports you to the lobby");
        this.setUsage("/lobby");
        this.setAliases(Arrays.asList("hub", "spawn", "l", "leave", "exit", "return", "lobbyspawn"));
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "command.player-only");
            return true;
        }
        Player player = (Player) sender;

        // Retrieve the lobby location from the configuration
        String worldName = plugin.getConfig().getString("lobby.world");
        if (worldName == null) {
            sendMessage(player, "command.lobby.not-set");
            return true;
        }

        double x = plugin.getConfig().getDouble("lobby.x");
        double y = plugin.getConfig().getDouble("lobby.y");
        double z = plugin.getConfig().getDouble("lobby.z");
        float yaw = (float) plugin.getConfig().getDouble("lobby.yaw");
        float pitch = (float) plugin.getConfig().getDouble("lobby.pitch");

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sendMessage(player, "command.lobby.world-not-found");
            return true;
        }

        Location lobbyLoc = new Location(world, x, y, z, yaw, pitch);
        player.teleport(lobbyLoc);
        sendMessage(player, "command.lobby.teleported");
        return true;
    }
}
