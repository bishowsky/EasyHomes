package com.easyhomes.commands;

import com.easyhomes.manager.HomeManager;
import com.easyhomes.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DelHomeCommand implements CommandExecutor, TabCompleter {
    private final HomeManager homeManager;
    private final FileConfiguration config;

    public DelHomeCommand(HomeManager homeManager, FileConfiguration config) {
        this.homeManager = homeManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("easyhomes.delhome")) {
            player.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(getMessage("usage-delhome"));
            return true;
        }

        String homeName = args[0].toLowerCase();

        // Check if home exists
        if (!homeManager.hasHome(player, homeName)) {
            player.sendMessage(getMessage("delhome-not-found", "home", homeName));
            return true;
        }

        // Delete the home
        homeManager.deleteHome(player, homeName);
        player.sendMessage(getMessage("delhome-success", "home", homeName));

        return true;
    }

    private String getMessage(String key, Object... replacements) {
        String prefix = config.getString("messages.prefix", "&8[&6EasyHomes&8]&r ");
        String message = config.getString("messages." + key, "&cMessage not found: " + key);
        return MessageUtil.format(prefix + message, replacements);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            // Suggest home names
            List<String> homeNames = new ArrayList<>(homeManager.getHomes(player).keySet());
            String prefix = args[0].toLowerCase();
            homeNames.removeIf(home -> !home.toLowerCase().startsWith(prefix));
            return homeNames;
        }

        return new ArrayList<>();
    }
}
