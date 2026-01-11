package com.easyhomes.commands;

import com.easyhomes.util.DebugManager;
import com.easyhomes.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EasyHomesCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final DebugManager debugManager;

    public EasyHomesCommand(JavaPlugin plugin, FileConfiguration config, DebugManager debugManager) {
        this.plugin = plugin;
        this.config = config;
        this.debugManager = debugManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getMessage("usage-easyhomes"));
            return true;
        }

        String subcommand = args[0].toLowerCase();

        // Komenda /easyhomes reload
        if (subcommand.equals("reload")) {
            if (!sender.hasPermission("easyhomes.reload")) {
                sender.sendMessage(getMessage("reload-no-permission"));
                return true;
            }

            plugin.reloadConfig();
            sender.sendMessage(getMessage("reload-success"));
            
            if (debugManager.isEnabled()) {
                debugManager.log("Config reloaded by " + sender.getName());
            }
            
            return true;
        }

        // Komenda /easyhomes debug <on|off>
        if (subcommand.equals("debug")) {
            if (!sender.hasPermission("easyhomes.debug")) {
                sender.sendMessage(getMessage("debug-no-permission"));
                return true;
            }

            if (args.length < 2) {
                // Toggle debug mode
                debugManager.toggle();
                
                if (debugManager.isEnabled()) {
                    String path = debugManager.getLogFilePath();
                    sender.sendMessage(getMessage("debug-enabled", "{path}", path));
                } else {
                    sender.sendMessage(getMessage("debug-disabled"));
                }
            } else {
                String mode = args[1].toLowerCase();
                
                if (mode.equals("on") || mode.equals("true") || mode.equals("enable")) {
                    debugManager.enable();
                    String path = debugManager.getLogFilePath();
                    sender.sendMessage(getMessage("debug-enabled", "{path}", path));
                } else if (mode.equals("off") || mode.equals("false") || mode.equals("disable")) {
                    debugManager.disable();
                    sender.sendMessage(getMessage("debug-disabled"));
                } else {
                    sender.sendMessage(getMessage("usage-easyhomes"));
                }
            }
            
            return true;
        }

        sender.sendMessage(getMessage("usage-easyhomes"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            
            if (sender.hasPermission("easyhomes.reload")) {
                subcommands.add("reload");
            }
            
            if (sender.hasPermission("easyhomes.debug")) {
                subcommands.add("debug");
            }

            // Filter based on what player typed
            String input = args[0].toLowerCase();
            completions = subcommands.stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            if (sender.hasPermission("easyhomes.debug")) {
                List<String> options = Arrays.asList("on", "off", "enable", "disable");
                String input = args[1].toLowerCase();
                completions = options.stream()
                        .filter(s -> s.startsWith(input))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }

    private String getMessage(String key, Object... replacements) {
        String prefix = config.getString("messages.prefix", "&8[&6EasyHomes&8]&r ");
        String message = config.getString("messages." + key, "&cWiadomość nie znaleziona: " + key);
        return MessageUtil.format(prefix + message, replacements);
    }
}
