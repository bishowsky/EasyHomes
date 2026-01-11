package com.easyhomes.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Manages MySQL database connections using HikariCP pool
 * Handles table creation and connection lifecycle
 */
public class DatabaseManager {
    private final Plugin plugin;
    private final FileConfiguration config;
    private HikariDataSource dataSource;
    private boolean enabled;

    public DatabaseManager(Plugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        this.enabled = false;
    }

    /**
     * Initialize database connection and create tables
     */
    public boolean initialize() {
        String storageType = config.getString("storage.type", "YAML").toUpperCase();
        
        if (!storageType.equals("MYSQL")) {
            plugin.getLogger().info("Storage type: YAML (fallback mode)");
            enabled = false;
            return false;
        }

        try {
            HikariConfig hikariConfig = new HikariConfig();
            
            // Connection settings
            String host = config.getString("storage.mysql.host", "localhost");
            int port = config.getInt("storage.mysql.port", 3306);
            String database = config.getString("storage.mysql.database", "minecraft");
            String username = config.getString("storage.mysql.username", "root");
            String password = config.getString("storage.mysql.password", "");
            boolean useSSL = config.getBoolean("storage.mysql.use-ssl", false);
            
            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL + "&autoReconnect=true");
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            
            // Pool settings
            hikariConfig.setMaximumPoolSize(config.getInt("storage.mysql.pool-size", 10));
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setConnectionTimeout(config.getInt("storage.mysql.connection-timeout", 5000));
            hikariConfig.setIdleTimeout(600000); // 10 minutes
            hikariConfig.setMaxLifetime(1800000); // 30 minutes
            hikariConfig.setLeakDetectionThreshold(60000); // 1 minute
            
            // Connection test
            hikariConfig.setConnectionTestQuery("SELECT 1");
            
            // Pool name
            hikariConfig.setPoolName("EasyHomes-Pool");
            
            // Additional properties for MySQL optimization
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
            hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
            hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
            hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
            hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
            hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
            hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
            
            dataSource = new HikariDataSource(hikariConfig);
            
            // Test connection
            try (Connection conn = dataSource.getConnection()) {
                plugin.getLogger().info("MySQL connection established successfully!");
            }
            
            // Create tables
            createTables();
            
            enabled = true;
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize MySQL database! Falling back to YAML storage.");
            plugin.getLogger().warning("Please check your MySQL configuration in config.yml");
            plugin.getLogger().warning("Error: " + e.getMessage());
            
            // Close dataSource if it was partially initialized
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                dataSource = null;
            }
            
            enabled = false;
            return false;
        }
    }

    /**
     * Create database tables if they don't exist
     */
    private void createTables() {
        String homesTable = "CREATE TABLE IF NOT EXISTS `easyhomes_homes` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`player_uuid` VARCHAR(36) NOT NULL," +
                "`home_name` VARCHAR(32) NOT NULL," +
                "`world` VARCHAR(64) NOT NULL," +
                "`x` DOUBLE NOT NULL," +
                "`y` DOUBLE NOT NULL," +
                "`z` DOUBLE NOT NULL," +
                "`yaw` FLOAT NOT NULL," +
                "`pitch` FLOAT NOT NULL," +
                "`created_at` BIGINT NOT NULL," +
                "`updated_at` BIGINT NOT NULL," +
                "UNIQUE KEY `player_home` (`player_uuid`, `home_name`)," +
                "INDEX `idx_player_uuid` (`player_uuid`)," +
                "INDEX `idx_home_name` (`home_name`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        String playersTable = "CREATE TABLE IF NOT EXISTS `easyhomes_players` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`uuid` VARCHAR(36) NOT NULL UNIQUE," +
                "`name` VARCHAR(16) NOT NULL," +
                "`last_seen` BIGINT NOT NULL," +
                "INDEX `idx_uuid` (`uuid`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        String statsTable = "CREATE TABLE IF NOT EXISTS `easyhomes_statistics` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`player_uuid` VARCHAR(36) NOT NULL," +
                "`home_name` VARCHAR(32) NOT NULL," +
                "`teleport_count` INT DEFAULT 0," +
                "`last_visited` BIGINT," +
                "UNIQUE KEY `player_home_stats` (`player_uuid`, `home_name`)," +
                "INDEX `idx_player_stats` (`player_uuid`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection conn = getConnection()) {
            // Create homes table
            try (PreparedStatement stmt = conn.prepareStatement(homesTable)) {
                stmt.executeUpdate();
            }
            
            // Create players table
            try (PreparedStatement stmt = conn.prepareStatement(playersTable)) {
                stmt.executeUpdate();
            }
            
            // Create statistics table
            try (PreparedStatement stmt = conn.prepareStatement(statsTable)) {
                stmt.executeUpdate();
            }
            
            plugin.getLogger().info("Database tables created/verified successfully!");
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create database tables!", e);
        }
    }

    /**
     * Get a connection from the pool
     */
    public Connection getConnection() throws SQLException {
        if (!enabled || dataSource == null) {
            throw new SQLException("Database is not enabled or initialized");
        }
        return dataSource.getConnection();
    }

    /**
     * Check if database is enabled and ready
     */
    public boolean isEnabled() {
        return enabled && dataSource != null && !dataSource.isClosed();
    }

    /**
     * Close database connection pool
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("MySQL connection pool closed.");
        }
    }

    /**
     * Execute async database operation
     */
    public void executeAsync(DatabaseOperation operation) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection()) {
                operation.execute(conn);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Database operation failed", e);
            }
        });
    }

    @FunctionalInterface
    public interface DatabaseOperation {
        void execute(Connection connection) throws SQLException;
    }
}
