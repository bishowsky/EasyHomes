package com.easyhomes.storage;

import com.easyhomes.model.Home;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HomeStorage {
    private final Plugin plugin;
    private final File homesFolder;
    private final Map<UUID, Map<String, Home>> cachedHomes;

    public HomeStorage(Plugin plugin) {
        this.plugin = plugin;
        this.homesFolder = new File(plugin.getDataFolder(), "homes");
        this.cachedHomes = new ConcurrentHashMap<>();

        if (!homesFolder.exists()) {
            homesFolder.mkdirs();
        }
    }

    /**
     * Get player homes file
     */
    private File getPlayerFile(UUID playerId) {
        return new File(homesFolder, playerId.toString() + ".yml");
    }

    /**
     * Load player homes from disk
     */
    public Map<String, Home> loadHomes(UUID playerId) {
        Map<String, Home> homes = cachedHomes.get(playerId);
        if (homes != null) {
            return new HashMap<>(homes);
        }

        homes = new HashMap<>();
        File file = getPlayerFile(playerId);

        if (!file.exists()) {
            cachedHomes.put(playerId, homes);
            return homes;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection homesSection = config.getConfigurationSection("homes");

        if (homesSection != null) {
            for (String homeName : homesSection.getKeys(false)) {
                ConfigurationSection homeSection = homesSection.getConfigurationSection(homeName);
                if (homeSection != null) {
                    Home home = Home.load(homeName, homeSection);
                    homes.put(homeName.toLowerCase(), home);
                }
            }
        }

        cachedHomes.put(playerId, homes);
        return new HashMap<>(homes);
    }

    /**
     * Save player homes to disk (async)
     */
    public void saveHomes(UUID playerId, Map<String, Home> homes) {
        // Update cache
        cachedHomes.put(playerId, new HashMap<>(homes));

        // Save async to prevent lag
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            File file = getPlayerFile(playerId);
            YamlConfiguration config = new YamlConfiguration();

            ConfigurationSection homesSection = config.createSection("homes");
            for (Map.Entry<String, Home> entry : homes.entrySet()) {
                ConfigurationSection homeSection = homesSection.createSection(entry.getKey());
                entry.getValue().save(homeSection);
            }

            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save homes for player " + playerId + ": " + e.getMessage());
            }
        });
    }

    /**
     * Get a specific home for a player
     */
    public Home getHome(UUID playerId, String homeName) {
        Map<String, Home> homes = loadHomes(playerId);
        return homes.get(homeName.toLowerCase());
    }

    /**
     * Add or update a home for a player
     */
    public void setHome(UUID playerId, Home home) {
        Map<String, Home> homes = loadHomes(playerId);
        homes.put(home.getName().toLowerCase(), home);
        saveHomes(playerId, homes);
    }

    /**
     * Remove a home for a player
     */
    public void deleteHome(UUID playerId, String homeName) {
        Map<String, Home> homes = loadHomes(playerId);
        homes.remove(homeName.toLowerCase());
        saveHomes(playerId, homes);
    }

    /**
     * Get all homes for a player
     */
    public Map<String, Home> getHomes(UUID playerId) {
        return loadHomes(playerId);
    }

    /**
     * Clear cache for a player (useful when player logs out)
     */
    public void unloadPlayer(UUID playerId) {
        cachedHomes.remove(playerId);
    }
}
