package com.easyhomes.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private final Map<UUID, Long> cooldowns;
    private final int defaultCooldown;
    private final Map<String, Integer> groupCooldowns;

    public CooldownManager(int defaultCooldown, Map<String, Integer> groupCooldowns) {
        this.cooldowns = new HashMap<>();
        this.defaultCooldown = defaultCooldown;
        this.groupCooldowns = groupCooldowns;
    }

    /**
     * Get cooldown duration for a player based on permissions
     */
    public int getCooldownDuration(Player player) {
        if (player.hasPermission("easyhomes.bypass.cooldown")) {
            return 0;
        }

        int duration = defaultCooldown;

        // Check group cooldowns
        for (Map.Entry<String, Integer> entry : groupCooldowns.entrySet()) {
            if (player.hasPermission("easyhomes.cooldown." + entry.getKey())) {
                duration = Math.min(duration, entry.getValue());
            }
        }

        return duration;
    }

    /**
     * Check if player is on cooldown
     */
    public boolean isOnCooldown(Player player) {
        return getRemainingCooldown(player) > 0;
    }

    /**
     * Get remaining cooldown time in seconds
     */
    public long getRemainingCooldown(Player player) {
        if (player.hasPermission("easyhomes.bypass.cooldown")) {
            return 0;
        }

        Long lastUse = cooldowns.get(player.getUniqueId());
        if (lastUse == null) {
            return 0;
        }

        int cooldownDuration = getCooldownDuration(player);
        long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
        long remaining = cooldownDuration - elapsed;

        return Math.max(0, remaining);
    }

    /**
     * Set cooldown for a player
     */
    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Clear cooldown for a player
     */
    public void clearCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    /**
     * Clear all cooldowns
     */
    public void clearAll() {
        cooldowns.clear();
    }
}
