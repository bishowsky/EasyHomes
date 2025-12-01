package com.easyhomes.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {
    private final Map<UUID, Long> combatTags;
    private final int combatDuration;
    private final boolean enabled;

    public CombatManager(boolean enabled, int combatDuration) {
        this.combatTags = new HashMap<>();
        this.enabled = enabled;
        this.combatDuration = combatDuration;
    }

    /**
     * Tag a player as in combat
     */
    public void tagPlayer(Player player) {
        if (!enabled) {
            return;
        }
        combatTags.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Check if player is in combat
     */
    public boolean isInCombat(Player player) {
        if (!enabled || player.hasPermission("easyhomes.bypass.combat")) {
            return false;
        }

        Long tagTime = combatTags.get(player.getUniqueId());
        if (tagTime == null) {
            return false;
        }

        long elapsed = (System.currentTimeMillis() - tagTime) / 1000;
        if (elapsed >= combatDuration) {
            combatTags.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    /**
     * Get remaining combat tag time in seconds
     */
    public long getRemainingCombatTime(Player player) {
        if (!enabled || player.hasPermission("easyhomes.bypass.combat")) {
            return 0;
        }

        Long tagTime = combatTags.get(player.getUniqueId());
        if (tagTime == null) {
            return 0;
        }

        long elapsed = (System.currentTimeMillis() - tagTime) / 1000;
        long remaining = combatDuration - elapsed;

        if (remaining <= 0) {
            combatTags.remove(player.getUniqueId());
            return 0;
        }

        return remaining;
    }

    /**
     * Remove combat tag from player
     */
    public void removeTag(Player player) {
        combatTags.remove(player.getUniqueId());
    }

    /**
     * Clear all combat tags
     */
    public void clearAll() {
        combatTags.clear();
    }

    /**
     * Check if combat tagging is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
}
