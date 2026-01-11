package com.easyhomes.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import com.easyhomes.manager.HomeManager;
import com.easyhomes.manager.CooldownManager;
import com.easyhomes.manager.CombatManager;
import com.easyhomes.model.Home;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import java.util.Map;

/**
 * PlaceholderAPI expansion for EasyHomes
 * Provides placeholders for home count, limits, cooldowns, etc.
 */
public class EasyHomesExpansion extends PlaceholderExpansion {
    private final HomeManager homeManager;
    private final CooldownManager cooldownManager;
    private final CombatManager combatManager;

    public EasyHomesExpansion(HomeManager homeManager, CooldownManager cooldownManager, CombatManager combatManager) {
        this.homeManager = homeManager;
        this.cooldownManager = cooldownManager;
        this.combatManager = combatManager;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "easyhomes";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "Bishyy";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return "";
        }

        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return "";
        }

        // %easyhomes_count% - Current home count
        if (params.equalsIgnoreCase("count")) {
            int count = homeManager.getHomes(player).size();
            return String.valueOf(count);
        }

        // %easyhomes_limit% - Max home limit
        if (params.equalsIgnoreCase("limit")) {
            int limit = homeManager.getHomeLimit(player);
            return limit == -1 ? "∞" : String.valueOf(limit);
        }

        // %easyhomes_available% - Remaining home slots
        if (params.equalsIgnoreCase("available")) {
            int count = homeManager.getHomes(player).size();
            int limit = homeManager.getHomeLimit(player);
            
            if (limit == -1) {
                return "∞";
            }
            
            int available = limit - count;
            return String.valueOf(Math.max(0, available));
        }

        // %easyhomes_list% - Comma-separated list of home names
        if (params.equalsIgnoreCase("list")) {
            Map<String, Home> homes = homeManager.getHomes(player);
            
            if (homes.isEmpty()) {
                return "Brak";
            }
            
            return String.join(", ", homes.keySet());
        }

        // %easyhomes_cooldown% - Remaining cooldown time
        if (params.equalsIgnoreCase("cooldown")) {
            long cooldown = cooldownManager.getRemainingCooldown(player);
            
            if (cooldown <= 0) {
                return "0";
            }
            
            return String.valueOf(cooldown);
        }

        // %easyhomes_combat% - Remaining combat tag time
        if (params.equalsIgnoreCase("combat")) {
            if (!combatManager.isInCombat(player)) {
                return "0";
            }
            
            long remaining = combatManager.getRemainingCombatTime(player);
            return String.valueOf(Math.max(0, remaining));
        }

        // %easyhomes_has_homes% - true/false if player has homes
        if (params.equalsIgnoreCase("has_homes")) {
            boolean hasHomes = !homeManager.getHomes(player).isEmpty();
            return String.valueOf(hasHomes);
        }

        // %easyhomes_has_cooldown% - true/false if player has cooldown
        if (params.equalsIgnoreCase("has_cooldown")) {
            boolean hasCooldown = cooldownManager.getRemainingCooldown(player) > 0;
            return String.valueOf(hasCooldown);
        }

        // %easyhomes_in_combat% - true/false if player is in combat
        if (params.equalsIgnoreCase("in_combat")) {
            boolean inCombat = combatManager.isInCombat(player);
            return String.valueOf(inCombat);
        }

        // %easyhomes_at_limit% - true/false if player reached limit
        if (params.equalsIgnoreCase("at_limit")) {
            int count = homeManager.getHomes(player).size();
            int limit = homeManager.getHomeLimit(player);
            
            if (limit == -1) {
                return "false";
            }
            
            return String.valueOf(count >= limit);
        }

        return null;
    }
}
