package dev.soulfur.hydrogen.command.commands;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.command.CommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class FlyCommand extends CommandHandler {

    public FlyCommand(Hydrogen plugin) {
        super("fly", plugin);
        this.setPermission("hydrogen.vip.fly");
        this.setDescription("Toggle flight");
        this.setUsage("/fly");
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        // Only players can use this command
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "command.player-only");
            return true;
        }

        // No arguments should be passed
        if (args.length > 0) {
            sendMessage(player, "command.fly.invalid-usage");
            return true;
        }

        // Toggle flight state
        boolean currentState = player.getAllowFlight();
        boolean newState = !currentState;
        player.setAllowFlight(newState);
        player.setFlying(newState);

        // Save the new flight state to the database.
        plugin.getDatabaseManager().setFlying(player.getUniqueId(), newState);

        String state = newState ? "enabled" : "disabled";
        sendMessage(player, "command.fly.toggled", Map.of("state", state));
        return true;
    }

    @Override
    protected boolean requiresPlayer() {
        return true;
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of(); // No tab-completions needed
    }
}
