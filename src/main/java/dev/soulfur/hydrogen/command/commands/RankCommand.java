package dev.soulfur.hydrogen.command.commands;

import dev.soulfur.hydrogen.Hydrogen;
import dev.soulfur.hydrogen.modules.rank.RankModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {

    private final Hydrogen plugin;
    private final RankModule rankModule;

    public RankCommand(Hydrogen plugin, RankModule rankModule) {
        this.plugin = plugin;
        this.rankModule = rankModule;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure that the command is executed by a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be executed by a player.").color(NamedTextColor.RED));
            return true;
        }
        Player player = (Player) sender;

        // No arguments: display the player's current rank.
        if (args.length == 0) {
            String rankPrefix = rankModule.getRankPrefix(player);
            player.sendMessage(Component.text("Your current rank is: " + rankPrefix)
                    .color(NamedTextColor.YELLOW));
            return true;
        }

        // With arguments: attempt to change the player's rank.
        // For example: /rank admin
        if (args.length >= 1) {
            // You might restrict this action to players with a specific permission.
            if (!player.hasPermission("hydrogen.rank.change")) {
                player.sendMessage(Component.text("You do not have permission to change your rank.")
                        .color(NamedTextColor.RED));
                return true;
            }
            String newRank = args[0].toLowerCase();
            // Here you might want to validate the new rank against allowed values (e.g., defined in your ranks.yml).
            rankModule.setPlayerRank(player, newRank);
            player.sendMessage(Component.text("Your rank has been updated to: " + newRank)
                    .color(NamedTextColor.GREEN));
            return true;
        }

        return false;
    }
}
