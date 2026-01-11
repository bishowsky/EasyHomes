package com.easyhomes.storage;

import com.easyhomes.database.DatabaseManager;
import com.easyhomes.database.MySQLStorage;
import com.easyhomes.model.Home;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class HomeStorage {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final File homesFolder;
    private final Cache<UUID, Map<String, Home>> cache;
    private final DatabaseManager databaseManager;
    private final MySQLStorage mysqlStorage;
    private final boolean useMysql;

    public HomeStorage(Plugin plugin, FileConfiguration config, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.config = config;
        this.databaseManager = databaseManager;
        this.homesFolder = new File(plugin.getDataFolder(), "homes");
        this.useMysql = databaseManager != null && databaseManager.isEnabled();
        
        if (!homesFolder.exists()) {
            homesFolder.mkdirs();
        }

        // Initialize Guava cache with TTL and max size
        int cacheTTL = config.getInt("cache.ttl-seconds", 300); // 5 minutes default
        int cacheMaxSize = config.getInt("cache.max-size", 10000);
        
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(cacheTTL, TimeUnit.SECONDS)
                .maximumSize(cacheMaxSize)
                .build();

        // Initialize MySQL storage if available
        this.mysqlStorage = useMysql ? new MySQLStorage(plugin, databaseManager) : null;
        
        if (useMysql) {
            plugin.getLogger().info("Storage: MySQL (with cache TTL: " + cacheTTL + "s, max: " + cacheMaxSize + ")");
        } else {
            plugin.getLogger().warning("Storage: YAML fallback mode (MySQL not configured or unavailable)");
        }
    }

    /**
     * Get player homes file
     */
    private File getPlayerFile(UUID playerId) {
        return new File(homesFolder, playerId.toString() + ".yml");
    }

    /**
     * Load player homes from cache or storage
     */
    public Map<String, Home> loadHomes(UUID playerId) {
        // Try cache first
        Map<String, Home> cached = cache.getIfPresent(playerId);
        if (cached != null) {
            return new HashMap<>(cached);
        }

        // Load from storage
        Map<String, Home> homes;
        
        if (useMysql) {
            homes = loadFromMySQL(playerId);
        } else {
            homes = loadFromYAML(playerId);
        }

        // Update cache
        cache.put(playerId, new HashMap<>(homes));
        
        return homes;
    }

    /**
     * Load homes from MySQL
     */
    private Map<String, Home> loadFromMySQL(UUID playerId) {
        try {
            return mysqlStorage.loadHomes(playerId).get();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load homes from MySQL, falling back to YAML", e);
            return loadFromYAML(playerId);
        }
    }

    /**
     * Load homes from YAML file
     */
    private Map<String, Home> loadFromYAML(UUID playerId) {
        Map<String, Home> homes = new HashMap<>();
        File file = getPlayerFile(playerId);

        if (!file.exists()) {
            return homes;
        }

        YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection homesSection = yamlConfig.getConfigurationSection("homes");

        if (homesSection != null) {
            for (String homeName : homesSection.getKeys(false)) {
                ConfigurationSection homeSection = homesSection.getConfigurationSection(homeName);
                if (homeSection != null) {
                    try {
                        Home home = Home.load(homeName, homeSection);
                        homes.put(homeName.toLowerCase(), home);
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Failed to load home '" + homeName + "' for player " + playerId, e);
                    }
                }
            }
        }

        return homes;
    }

    /**
     * Save player homes to storage (async)
     */
    public void saveHomes(UUID playerId, Map<String, Home> homes) {
        // Update cache immediately
        cache.put(playerId, new HashMap<>(homes));

        // Save async to prevent lag
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (useMysql) {
                saveToMySQL(playerId, homes);
            } else {
                saveToYAML(playerId, homes);
            }
        });
    }

    /**
     * Save homes to MySQL
     */
    private void saveToMySQL(UUID playerId, Map<String, Home> homes) {
        // This method is called for each individual home, so we just save one
        // The actual saving is handled by setHome/deleteHome methods
    }

    /**
     * Save homes to YAML file
     */
    private void saveToYAML(UUID playerId, Map<String, Home> homes) {
        File file = getPlayerFile(playerId);
        YamlConfiguration yamlConfig = new YamlConfiguration();

        ConfigurationSection homesSection = yamlConfig.createSection("homes");
        for (Map.Entry<String, Home> entry : homes.entrySet()) {
            ConfigurationSection homeSection = homesSection.createSection(entry.getKey());
            entry.getValue().save(homeSection);
        }

        try {
            yamlConfig.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save homes for player " + playerId, e);
        }
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
        
        // Save to cache and storage
        cache.put(playerId, new HashMap<>(homes));
        
        if (useMysql) {
            mysqlStorage.saveHome(playerId, home);
        } else {
            saveToYAML(playerId, homes);
        }
    }

    /**
     * Remove a home for a player
     */
    public void deleteHome(UUID playerId, String homeName) {
        Map<String, Home> homes = loadHomes(playerId);
        homes.remove(homeName.toLowerCase());
        
        // Update cache
        cache.put(playerId, new HashMap<>(homes));
        
        if (useMysql) {
            mysqlStorage.deleteHome(playerId, homeName);
        } else {
            saveToYAML(playerId, homes);
        }
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
        cache.invalidate(playerId);
    }

    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        return String.format("Size: %d, Hit rate: %.2f%%", 
            cache.size(), 
            cache.stats().hitRate() * 100);
    }

    /**
     * Clear all cache
     */
    public void clearCache() {
        cache.invalidateAll();
        plugin.getLogger().info("Cache cleared");
    }
}
