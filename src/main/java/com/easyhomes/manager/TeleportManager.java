package com.easyhomes.manager;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    private final Plugin plugin;
    private final Map<UUID, PendingTeleport> pendingTeleports;

    // Configuration
    private final int teleportDelay;
    private final boolean cancelOnMove;
    private final boolean particlesEnabled;
    private final String particleType;
    private final int particleAmount;
    private final boolean showParticlesAtStart;
    private final boolean showParticlesAtDestination;
    private final boolean soundsEnabled;
    private final String soundType;
    private final float soundVolume;
    private final float soundPitch;
    private final boolean titleEnabled;
    private final String titleText;
    private final String subtitleText;

    public TeleportManager(Plugin plugin, int teleportDelay, boolean cancelOnMove,
            boolean particlesEnabled, String particleType, int particleAmount,
            boolean showParticlesAtStart, boolean showParticlesAtDestination,
            boolean soundsEnabled, String soundType, float soundVolume, float soundPitch,
            boolean titleEnabled, String titleText, String subtitleText) {
        this.plugin = plugin;
        this.pendingTeleports = new HashMap<>();
        this.teleportDelay = teleportDelay;
        this.cancelOnMove = cancelOnMove;
        this.particlesEnabled = particlesEnabled;
        this.particleType = particleType;
        this.particleAmount = particleAmount;
        this.showParticlesAtStart = showParticlesAtStart;
        this.showParticlesAtDestination = showParticlesAtDestination;
        this.soundsEnabled = soundsEnabled;
        this.soundType = soundType;
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
        this.titleEnabled = titleEnabled;
        this.titleText = titleText;
        this.subtitleText = subtitleText;
    }

    /**
     * Start a delayed teleport
     */
    public void teleport(Player player, Location destination, Runnable onSuccess, Runnable onCancel) {
        // Cancel any existing teleport
        cancelTeleport(player);

        Location startLocation = player.getLocation().clone();

        // Show particles at start location
        if (particlesEnabled && showParticlesAtStart) {
            spawnParticles(startLocation);
        }

        // If no delay, teleport immediately
        if (teleportDelay <= 0) {
            performTeleport(player, destination);
            if (onSuccess != null) {
                onSuccess.run();
            }
            return;
        }

        // Store pending teleport
        PendingTeleport pending = new PendingTeleport(player, destination, startLocation, onSuccess, onCancel);
        pendingTeleports.put(player.getUniqueId(), pending);

        // Schedule teleport
        pending.task = new BukkitRunnable() {
            int countdown = teleportDelay;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    pendingTeleports.remove(player.getUniqueId());
                    return;
                }

                // Check if player moved
                if (cancelOnMove && hasMoved(player, startLocation)) {
                    cancel();
                    pendingTeleports.remove(player.getUniqueId());
                    if (onCancel != null) {
                        onCancel.run();
                    }
                    return;
                }

                countdown--;

                // Send title if enabled
                if (titleEnabled && countdown > 0) {
                    String title = ChatColor.translateAlternateColorCodes('&', titleText);
                    String subtitle = ChatColor.translateAlternateColorCodes('&', subtitleText.replace("{time}", String.valueOf(countdown)));
                    player.sendTitle(title, subtitle);
                }

                if (countdown <= 0) {
                    cancel();
                    pendingTeleports.remove(player.getUniqueId());
                    performTeleport(player, destination);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    /**
     * Perform the actual teleportation with effects
     */
    private void performTeleport(Player player, Location destination) {
        player.teleport(destination);

        // Show particles at destination
        if (particlesEnabled && showParticlesAtDestination) {
            spawnParticles(destination);
        }

        // Play sound
        if (soundsEnabled) {
            playSound(player, destination);
        }
    }

    /**
     * Spawn particle effects at a location
     */
    private void spawnParticles(Location location) {
        try {
            // Try to use Effect enum for 1.8 compatibility
            Effect effect = Effect.valueOf(particleType);
            location.getWorld().playEffect(location, effect, null, 50);
        } catch (IllegalArgumentException e) {
            // Fallback to default particle effect
            location.getWorld().playEffect(location, Effect.ENDER_SIGNAL, null, 50);
        }
    }

    /**
     * Play sound effect
     */
    private void playSound(Player player, Location location) {
        try {
            // Try to parse as Sound enum
            Sound sound = Sound.valueOf(soundType);
            player.playSound(location, sound, soundVolume, soundPitch);
        } catch (IllegalArgumentException e) {
            // Fallback for 1.8 compatibility - ENDERMAN_TELEPORT
            try {
                player.playSound(location, Sound.valueOf("ENDERMAN_TELEPORT"), soundVolume, soundPitch);
            } catch (Exception ex) {
                // If all fails, don't play sound
            }
        }
    }

    /**
     * Check if player has moved from the start location
     */
    private boolean hasMoved(Player player, Location start) {
        Location current = player.getLocation();
        return current.getBlockX() != start.getBlockX() ||
                current.getBlockY() != start.getBlockY() ||
                current.getBlockZ() != start.getBlockZ();
    }

    /**
     * Cancel a pending teleport
     */
    public void cancelTeleport(Player player) {
        PendingTeleport pending = pendingTeleports.remove(player.getUniqueId());
        if (pending != null && pending.task != null) {
            pending.task.cancel();
        }
    }

    /**
     * Check if player has a pending teleport
     */
    public boolean hasPendingTeleport(Player player) {
        return pendingTeleports.containsKey(player.getUniqueId());
    }

    /**
     * Clear all pending teleports
     */
    public void clearAll() {
        for (PendingTeleport pending : pendingTeleports.values()) {
            if (pending.task != null) {
                pending.task.cancel();
            }
        }
        pendingTeleports.clear();
    }

    /**
     * Class to store pending teleport data
     */
    private static class PendingTeleport {
        final Player player;
        final Location destination;
        final Location startLocation;
        final Runnable onSuccess;
        final Runnable onCancel;
        BukkitTask task;

        PendingTeleport(Player player, Location destination, Location startLocation,
                Runnable onSuccess, Runnable onCancel) {
            this.player = player;
            this.destination = destination;
            this.startLocation = startLocation;
            this.onSuccess = onSuccess;
            this.onCancel = onCancel;
        }
    }
}
