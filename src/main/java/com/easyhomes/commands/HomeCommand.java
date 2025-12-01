package com.easyhomes.commands;

import com.easyhomes.manager.CombatManager;
import com.easyhomes.manager.CooldownManager;
import com.easyhomes.manager.HomeManager;
import com.easyhomes.manager.TeleportManager;
import com.easyhomes.model.Home;
import com.easyhomes.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class HomeCommand implements CommandExecutor, TabCompleter {
    private final HomeManager homeManager;
    private final CooldownManager cooldownManager;
    private final CombatManager combatManager;
    private final TeleportManager teleportManager;
    private final FileConfiguration config;

    public HomeCommand(HomeManager homeManager, CooldownManager cooldownManager,
            CombatManager combatManager, TeleportManager teleportManager,
            FileConfiguration config) {
        this.homeManager = homeManager;
        this.cooldownManager = cooldownManager;
        this.combatManager = combatManager;
        this.teleportManager = teleportManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("easyhomes.home")) {
            player.sendMessage(getMessage("no-permission"));
            return true;
        }

        // If no arguments, list homes
        if (args.length == 0) {
            listHomes(player);
            return true;
        }

        String homeName = args[0].toLowerCase();
        Home home = homeManager.getHome(player, homeName);

        if (home == null) {
            player.sendMessage(getMessage("home-not-found", "home", homeName));
            return true;
        }

        // Check combat status
        if (combatManager.isInCombat(player)) {
            long remaining = combatManager.getRemainingCombatTime(player);
            player.sendMessage(getMessage("combat-active", "time", remaining));
            return true;
        }

        // Check cooldown
        if (cooldownManager.isOnCooldown(player)) {
            long remaining = cooldownManager.getRemainingCooldown(player);
            player.sendMessage(getMessage("cooldown-active", "time", remaining));
            return true;
        }

        // Get home location
        Location location = home.getLocation();
        if (location == null) {
            player.sendMessage(getMessage("home-not-found", "home", homeName));
            return true;
        }

        // Start teleport
        int delay = config.getInt("teleport.delay", 3);
        player.sendMessage(getMessage("home-teleporting", "home", homeName, "delay", delay));

        teleportManager.teleport(player, location,
                () -> {
                    // On success
                    player.sendMessage(getMessage("home-teleport-success", "home", homeName));
                    cooldownManager.setCooldown(player);
                },
                () -> {
                    // On cancel
                    player.sendMessage(getMessage("teleport-cancelled-move"));
                });

        return true;
    }

    private void listHomes(Player player) {
        Map<String, Home> homes = homeManager.getHomes(player);

        if (homes.isEmpty()) {
            player.sendMessage(getMessage("no-homes"));
            return;
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (String homeName : homes.keySet()) {
            joiner.add(homeName);
        }

        player.sendMessage(getMessage("home-list", "homes", joiner.toString()));
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
            Map<String, Home> homes = homeManager.getHomes(player);
            List<String> suggestions = new ArrayList<>(homes.keySet());
            String prefix = args[0].toLowerCase();
            suggestions.removeIf(home -> !home.toLowerCase().startsWith(prefix));
            return suggestions;
        }

        return new ArrayList<>();
    }
}
