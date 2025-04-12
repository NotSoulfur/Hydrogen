package dev.soulfur.hydrogen.command.commands;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.command.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlyCommand extends CommandHandler {

    public FlyCommand(Hydrogen plugin) {
        super("fly", plugin);
        this.setPermission("hydrogen.command.fly");
        this.setDescription("Toggle flight for yourself or another player");
        this.setUsage("/fly [player]");
        // Optionally add an alias if desired
        // this.setAliases(Arrays.asList("togglefly"));
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {

        // If a target player name is provided, toggle for that player
        if (args.length > 0) {
            // Check permission for toggling others' flight
            if (!sender.hasPermission("hydrogen.command.fly.others")) {
                sendMessage(sender, "command.no-permission");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendMessage(sender, "command.player-not-found");
                return true;
            }
            toggleFlight(target);

            // Prepare placeholders for messages
            String state = target.getAllowFlight() ? "enabled" : "disabled";
            // Notify the command sender
            sendMessage(sender, "command.fly.toggled-other", Map.of("player", target.getName(), "state", state));
            // Notify the target player
            sendMessage(target, "command.fly.toggled-by-other", Map.of("state", state));
        } else {
            // Toggling flight for the sender
            if (!(sender instanceof Player)) {
                sendMessage(sender, "command.player-only");
                return true;
            }
            Player player = (Player) sender;
            toggleFlight(player);
            String state = player.getAllowFlight() ? "enabled" : "disabled";
            sendMessage(player, "command.fly.toggled", Map.of("state", state));
        }
        return true;
    }

    /**
     * Toggles the flight state of a player.
     *
     * @param player the Player to toggle flight for
     */
    private void toggleFlight(Player player) {
        boolean currentState = player.getAllowFlight();
        // Toggle flight status
        player.setAllowFlight(!currentState);
        // Optionally update the flying state as well if you want to enforce flying immediately
        player.setFlying(!currentState);
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        // When tab-completing the first argument, if the sender has permission to toggle flight for others, suggest online player names
        if (args.length == 1 && sender.hasPermission("hydrogen.command.fly.others")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
            return filterCompletions(completions, args[0]);
        }

        return completions;
    }

    /**
     * Filters the provided list of completions so that only those that begin with the given input are returned.
     *
     * @param completions the List of potential completions
     * @param input       the string to match against
     * @return the filtered List of completions
     */
    private List<String> filterCompletions(List<String> completions, String input) {
        if (input.isEmpty()) {
            return completions;
        }
        List<String> filtered = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(completion);
            }
        }
        return filtered;
    }
}
