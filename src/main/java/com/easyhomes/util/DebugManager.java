package com.easyhomes.util;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * Manages debug logging to file and console
 * Can be toggled on/off with /easyhomes debug command
 */
public class DebugManager {
    private final Plugin plugin;
    private final File logFile;
    private final SimpleDateFormat dateFormat;
    private boolean enabled;

    public DebugManager(Plugin plugin) {
        this.plugin = plugin;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // Create logs directory
        File logsDir = new File(plugin.getDataFolder(), "logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        
        // Create debug log file with timestamp
        String fileName = "debug-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
        this.logFile = new File(logsDir, fileName);
        this.enabled = false;
    }

    /**
     * Enable debug mode
     */
    public void enable() {
        this.enabled = true;
        log("Debug mode enabled");
        plugin.getLogger().info("Debug mode enabled. Logs will be written to: " + logFile.getPath());
    }

    /**
     * Disable debug mode
     */
    public void disable() {
        log("Debug mode disabled");
        this.enabled = false;
        plugin.getLogger().info("Debug mode disabled.");
    }

    /**
     * Toggle debug mode
     */
    public void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }

    /**
     * Check if debug is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Log a debug message
     */
    public void log(String message) {
        if (!enabled) {
            return;
        }

        String timestamp = dateFormat.format(new Date());
        String logMessage = "[" + timestamp + "] " + message;

        // Log to console
        plugin.getLogger().info("[DEBUG] " + message);

        // Log to file
        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logMessage);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to write to debug log file", e);
        }
    }

    /**
     * Log a debug message with throwable
     */
    public void log(String message, Throwable throwable) {
        if (!enabled) {
            return;
        }

        log(message);
        log("Exception: " + throwable.getClass().getName() + ": " + throwable.getMessage());

        // Write stack trace to file
        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            throwable.printStackTrace(pw);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to write exception to debug log file", e);
        }
    }

    /**
     * Log command execution
     */
    public void logCommand(String player, String command, String[] args) {
        if (!enabled) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Command executed by ").append(player).append(": /").append(command);
        
        if (args.length > 0) {
            sb.append(" ");
            for (int i = 0; i < args.length; i++) {
                sb.append(args[i]);
                if (i < args.length - 1) {
                    sb.append(" ");
                }
            }
        }

        log(sb.toString());
    }

    /**
     * Log database operation
     */
    public void logDatabase(String operation, String details) {
        if (!enabled) {
            return;
        }

        log("[DATABASE] " + operation + " - " + details);
    }

    /**
     * Log cache operation
     */
    public void logCache(String operation, String details) {
        if (!enabled) {
            return;
        }

        log("[CACHE] " + operation + " - " + details);
    }

    /**
     * Log teleport operation
     */
    public void logTeleport(String player, String home, boolean success) {
        if (!enabled) {
            return;
        }

        String status = success ? "SUCCESS" : "FAILED";
        log("[TELEPORT] Player: " + player + ", Home: " + home + ", Status: " + status);
    }

    /**
     * Get log file path
     */
    public String getLogFilePath() {
        return logFile.getAbsolutePath();
    }
}
