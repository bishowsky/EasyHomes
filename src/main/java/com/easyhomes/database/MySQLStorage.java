package com.easyhomes.database;

import com.easyhomes.model.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * MySQL implementation of home storage
 * All operations are async to prevent main thread blocking
 */
public class MySQLStorage {
    private final Plugin plugin;
    private final DatabaseManager databaseManager;

    public MySQLStorage(Plugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    /**
     * Load all homes for a player from database
     */
    public CompletableFuture<Map<String, Home>> loadHomes(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Home> homes = new HashMap<>();
            
            String query = "SELECT * FROM easyhomes_homes WHERE player_uuid = ?";
            
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerId.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String homeName = rs.getString("home_name");
                        String worldName = rs.getString("world");
                        double x = rs.getDouble("x");
                        double y = rs.getDouble("y");
                        double z = rs.getDouble("z");
                        float yaw = rs.getFloat("yaw");
                        float pitch = rs.getFloat("pitch");
                        long createdAt = rs.getLong("created_at");
                        
                        World world = Bukkit.getWorld(worldName);
                        if (world == null) {
                            plugin.getLogger().warning("World '" + worldName + "' not found for home '" + homeName + "' of player " + playerId);
                            continue;
                        }
                        
                        Home home = new Home(homeName, worldName, x, y, z, yaw, pitch, createdAt);
                        homes.put(homeName.toLowerCase(), home);
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load homes for player " + playerId, e);
            }
            
            return homes;
        });
    }

    /**
     * Save or update a home in database
     */
    public CompletableFuture<Void> saveHome(UUID playerId, Home home) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO easyhomes_homes " +
                    "(player_uuid, home_name, world, x, y, z, yaw, pitch, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "world = VALUES(world), x = VALUES(x), y = VALUES(y), z = VALUES(z), " +
                    "yaw = VALUES(yaw), pitch = VALUES(pitch), updated_at = VALUES(updated_at)";
            
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                Location loc = home.getLocation();
                long now = System.currentTimeMillis();
                
                stmt.setString(1, playerId.toString());
                stmt.setString(2, home.getName());
                stmt.setString(3, loc.getWorld().getName());
                stmt.setDouble(4, loc.getX());
                stmt.setDouble(5, loc.getY());
                stmt.setDouble(6, loc.getZ());
                stmt.setFloat(7, loc.getYaw());
                stmt.setFloat(8, loc.getPitch());
                stmt.setLong(9, home.getCreatedAt());
                stmt.setLong(10, now);
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save home '" + home.getName() + "' for player " + playerId, e);
            }
        });
    }

    /**
     * Delete a home from database
     */
    public CompletableFuture<Void> deleteHome(UUID playerId, String homeName) {
        return CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM easyhomes_homes WHERE player_uuid = ? AND home_name = ?";
            
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerId.toString());
                stmt.setString(2, homeName);
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to delete home '" + homeName + "' for player " + playerId, e);
            }
        });
    }

    /**
     * Delete all homes for a player
     */
    public CompletableFuture<Void> deleteAllHomes(UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM easyhomes_homes WHERE player_uuid = ?";
            
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerId.toString());
                int deleted = stmt.executeUpdate();
                
                plugin.getLogger().info("Deleted " + deleted + " homes for player " + playerId);
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to delete all homes for player " + playerId, e);
            }
        });
    }

    /**
     * Update player info (name and last seen)
     */
    public CompletableFuture<Void> updatePlayerInfo(UUID playerId, String playerName) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO easyhomes_players (uuid, name, last_seen) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name), last_seen = VALUES(last_seen)";
            
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerId.toString());
                stmt.setString(2, playerName);
                stmt.setLong(3, System.currentTimeMillis());
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to update player info for " + playerId, e);
            }
        });
    }

    /**
     * Increment teleport count for statistics
     */
    public CompletableFuture<Void> incrementTeleportCount(UUID playerId, String homeName) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO easyhomes_statistics (player_uuid, home_name, teleport_count, last_visited) " +
                    "VALUES (?, ?, 1, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "teleport_count = teleport_count + 1, last_visited = VALUES(last_visited)";
            
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerId.toString());
                stmt.setString(2, homeName);
                stmt.setLong(3, System.currentTimeMillis());
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to increment teleport count", e);
            }
        });
    }

    /**
     * Get total home count across all players
     */
    public CompletableFuture<Integer> getTotalHomeCount() {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT COUNT(*) as count FROM easyhomes_homes";
            
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt("count");
                }
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to get total home count", e);
            }
            
            return 0;
        });
    }

    /**
     * Get total teleport count for a player
     */
    public CompletableFuture<Integer> getPlayerTeleportCount(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT SUM(teleport_count) as total FROM easyhomes_statistics WHERE player_uuid = ?";
            
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerId.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("total");
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to get teleport count for player " + playerId, e);
            }
            
            return 0;
        });
    }
}
