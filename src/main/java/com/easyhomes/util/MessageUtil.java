package com.easyhomes.util;

import org.bukkit.ChatColor;

public class MessageUtil {

    /**
     * Format a message with color codes
     */
    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Format a message with placeholders
     */
    public static String format(String message, Object... replacements) {
        String result = message;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                String placeholder = "{" + replacements[i] + "}";
                String value = String.valueOf(replacements[i + 1]);
                result = result.replace(placeholder, value);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', result);
    }
}
