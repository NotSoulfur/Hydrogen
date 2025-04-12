package dev.soulfur.hydrogen.command.commands;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.command.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamemodeCommand extends CommandHandler {

    public GamemodeCommand(Hydrogen plugin) {
        super("gamemode", plugin);
        this.setPermission("hydrogen.command.gamemode");
        this.setDescription("Change your or another player's gamemode");
        this.setUsage("/gamemode <mode> [player]");
        this.setAliases(Arrays.asList("gm"));
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sendMessage(sender, "command.gamemode.usage");
            return true;
        }

        GameMode gameMode = parseGameMode(args[0]);
        if (gameMode == null) {
            sendMessage(sender, "command.gamemode.invalid-mode");
            return true;
        }

        if (args.length > 1) {
            // Change another player's gamemode
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sendMessage(sender, "command.player-not-found");
                return true;
            }

            if (!sender.hasPermission("hydrogen.command.gamemode.others")) {
                sendMessage(sender, "command.no-permission");
                return true;
            }

            target.setGameMode(gameMode);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            placeholders.put("gamemode", formatGameMode(gameMode));

            sendMessage(sender, "command.gamemode.changed-other", placeholders);
            sendMessage(target, "command.gamemode.changed-by-other", Map.of("gamemode", formatGameMode(gameMode)));
        } else {
            // Change own gamemode
            if (!(sender instanceof Player)) {
                sendMessage(sender, "command.player-only");
                return true;
            }

            Player player = (Player) sender;
            player.setGameMode(gameMode);

            sendMessage(player, "command.gamemode.changed", Map.of("gamemode", formatGameMode(gameMode)));
        }

        return true;
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("survival");
            completions.add("creative");
            completions.add("adventure");
            completions.add("spectator");
            completions.add("0");
            completions.add("1");
            completions.add("2");
            completions.add("3");
            completions.add("s");
            completions.add("c");
            completions.add("a");
            completions.add("sp");
            return filterCompletions(completions, args[0]);
        } else if (args.length == 2 && sender.hasPermission("hydrogen.command.gamemode.others")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
            return filterCompletions(completions, args[1]);
        }

        return completions;
    }

    private GameMode parseGameMode(String input) {
        input = input.toLowerCase();

        switch (input) {
            case "survival":
            case "s":
            case "0":
                return GameMode.SURVIVAL;
            case "creative":
            case "c":
            case "1":
                return GameMode.CREATIVE;
            case "adventure":
            case "a":
            case "2":
                return GameMode.ADVENTURE;
            case "spectator":
            case "sp":
            case "3":
                return GameMode.SPECTATOR;
            default:
                return null;
        }
    }

    private String formatGameMode(GameMode gameMode) {
        return gameMode.name().charAt(0) + gameMode.name().substring(1).toLowerCase();
    }

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