package com.easyhomes.commands;

import com.easyhomes.hooks.VaultManager;
import com.easyhomes.hooks.WorldGuardHook;
import com.easyhomes.manager.HomeManager;
import com.easyhomes.model.Home;
import com.easyhomes.util.DebugManager;
import com.easyhomes.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetHomeCommand implements CommandExecutor, TabCompleter {
    private final HomeManager homeManager;
    private final VaultManager vaultManager;
    private final WorldGuardHook worldGuardHook;
    private final DebugManager debugManager;
    private final FileConfiguration config;

    public SetHomeCommand(HomeManager homeManager, VaultManager vaultManager, WorldGuardHook worldGuardHook, DebugManager debugManager, FileConfiguration config) {
        this.homeManager = homeManager;
        this.vaultManager = vaultManager;
        this.worldGuardHook = worldGuardHook;
        this.debugManager = debugManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("easyhomes.sethome")) {
            player.sendMessage(getMessage("no-permission"));
            return true;
        }

        // Use "home" as default name if no argument provided
        String homeName = (args.length == 0) ? "home" : args[0].toLowerCase();

        // Validate home name
        if (!homeManager.isValidHomeName(homeName)) {
            player.sendMessage(getMessage("sethome-invalid-name"));
            return true;
        }

        // Check WorldGuard permissions
        if (worldGuardHook != null && worldGuardHook.isEnabled()) {
            if (!worldGuardHook.canBuild(player, player.getLocation())) {
                player.sendMessage(getMessage("sethome-no-permission-region"));
                debugManager.log(player.getName() + " tried to set home in protected region");
                return true;
            }
        }

        // Check if updating existing home or creating new one
        boolean isUpdate = homeManager.hasHome(player, homeName);

        // Check home limit if creating new home
        if (!isUpdate && !homeManager.canSetMoreHomes(player)) {
            int limit = homeManager.getHomeLimit(player);
            player.sendMessage(getMessage("sethome-limit-reached", "limit", limit));
            return true;
        }

        // Check economy cost
        if (!isUpdate && vaultManager != null && vaultManager.isEnabled() && config.getBoolean("economy.enabled", false)) {
            double cost = config.getDouble("economy.sethome-cost", 0);
            
            if (cost > 0 && !player.hasPermission("easyhomes.bypass.cost")) {
                if (!vaultManager.has(player, cost)) {
                    player.sendMessage(getMessage("economy-insufficient-funds", "cost", vaultManager.format(cost)));
                    return true;
                }
                
                vaultManager.withdraw(player, cost);
                player.sendMessage(getMessage("economy-sethome-cost", "cost", vaultManager.format(cost)));
                debugManager.log(player.getName() + " paid " + cost + " for sethome");
            }
        }

        // Set the home
        homeManager.setHome(player, homeName, player.getLocation());
        debugManager.log(player.getName() + " set home: " + homeName + " at " + player.getLocation());

        if (isUpdate) {
            player.sendMessage(getMessage("sethome-updated", "home", homeName));
        } else {
            player.sendMessage(getMessage("sethome-success", "home", homeName));
        }

        return true;
    }

    private String getMessage(String key, Object... replacements) {
        String prefix = config.getString("messages.prefix", "&8[&6EasyHomes&8]&r ");
        String message = config.getString("messages." + key, "&cWiadomość nie znaleziona: " + key);
        return MessageUtil.format(prefix + message, replacements);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            // Suggest existing home names for updating
            List<String> homeNames = new ArrayList<>(homeManager.getHomes(player).keySet());
            String prefix = args[0].toLowerCase();
            homeNames.removeIf(home -> !home.toLowerCase().startsWith(prefix));
            return homeNames;
        }

        return new ArrayList<>();
    }
}
