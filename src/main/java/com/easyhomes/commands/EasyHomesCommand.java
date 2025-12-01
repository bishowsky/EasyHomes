package com.easyhomes.commands;

import com.easyhomes.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyHomesCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public EasyHomesCommand(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getMessage("usage-easyhomes"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("easyhomes.reload")) {
                sender.sendMessage(getMessage("reload-no-permission"));
                return true;
            }

            // Reload config
            plugin.reloadConfig();
            sender.sendMessage(getMessage("reload-success"));
            return true;
        }

        sender.sendMessage(getMessage("usage-easyhomes"));
        return true;
    }

    private String getMessage(String key, Object... replacements) {
        String prefix = config.getString("messages.prefix", "&8[&6EasyHomes&8]&r ");
        String message = config.getString("messages." + key, "&cMessage not found: " + key);
        return MessageUtil.format(prefix + message, replacements);
    }
}