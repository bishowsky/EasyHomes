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
     */
    public int getHomeLimit(Player player) {
        // Check for unlimited permission first
        if (config.getConfigurationSection("homes.limits") != null) {
            for (String key : config.getConfigurationSection("homes.limits").getKeys(false)) {
                String perm = config.getString("homes.limits." + key + ".permission");
                int limit = config.getInt("homes.limits." + key + ".limit");
                if (player.hasPermission(perm)) {
                    return limit == -1 ? Integer.MAX_VALUE : limit;
                }
            }
        }

        // Fallback to default limit
        return config.getInt("homes.default-limit", 1);
    }

    /**
     * Check if player can set more homes
     */
    public boolean canSetMoreHomes(Player player) {
        int current = getHomeCount(player);
        int limit = getHomeLimit(player);
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
