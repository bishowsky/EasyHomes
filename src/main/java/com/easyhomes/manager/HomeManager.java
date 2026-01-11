package com.easyhomes.manager;

import com.easyhomes.model.Home;
import com.easyhomes.storage.HomeStorage;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class HomeManager {
    private final HomeStorage storage;
    private final FileConfiguration config;
    private static final Pattern VALID_HOME_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

    public HomeManager(HomeStorage storage, FileConfiguration config) {
        this.storage = storage;
        this.config = config;
    }

    /**
     * Get maximum homes allowed for a player
     * Checks permissions in order: unlimited, 10, 5, 3, 1
     */
    public int getHomeLimit(Player player) {
        // Check for unlimited permission first
        if (player.hasPermission("easyhomes.unlimited")) {
            return -1; // Unlimited
        }
        
        // Check for numeric limits (highest to lowest)
        if (player.hasPermission("easyhomes.limit.50")) {
            return 50;
        }
        if (player.hasPermission("easyhomes.limit.25")) {
            return 25;
        }
        if (player.hasPermission("easyhomes.limit.15")) {
            return 15;
        }
        if (player.hasPermission("easyhomes.limit.10")) {
            return 10;
        }
        if (player.hasPermission("easyhomes.limit.5")) {
            return 5;
        }
        if (player.hasPermission("easyhomes.limit.3")) {
            return 3;
        }
        if (player.hasPermission("easyhomes.limit.1")) {
            return 1;
        }

        // Fallback to default limit from config
        return config.getInt("homes.default-limit", 1);
    }

    /**
     * Check if player can set more homes
     */
    public boolean canSetMoreHomes(Player player) {
        int current = getHomeCount(player);
        int limit = getHomeLimit(player);
        
        // -1 means unlimited
        if (limit == -1) {
            return true;
        }
        
        return current < limit;
    }

    /**
     * Get current home count for a player
     */
    public int getHomeCount(Player player) {
        return storage.getHomes(player.getUniqueId()).size();
    }

    /**
     * Validate home name
     */
    public boolean isValidHomeName(String name) {
        return name != null && VALID_HOME_NAME.matcher(name).matches() && name.length() <= 16;
    }

    /**
     * Set a home for a player
     */
    public void setHome(Player player, String homeName, Location location) {
        Home home = new Home(homeName, location);
        storage.setHome(player.getUniqueId(), home);
    }

    /**
     * Get a home for a player
     */
    public Home getHome(Player player, String homeName) {
        return storage.getHome(player.getUniqueId(), homeName);
    }

    /**
     * Delete a home for a player
     */
    public void deleteHome(Player player, String homeName) {
        storage.deleteHome(player.getUniqueId(), homeName);
    }

    /**
     * Get all homes for a player
     */
    public Map<String, Home> getHomes(Player player) {
        return storage.getHomes(player.getUniqueId());
    }

    /**
     * Check if a home exists
     */
    public boolean hasHome(Player player, String homeName) {
        return storage.getHome(player.getUniqueId(), homeName) != null;
    }

    /**
     * Unload player data
     */
    public void unloadPlayer(UUID playerId) {
        storage.unloadPlayer(playerId);
    }
}
