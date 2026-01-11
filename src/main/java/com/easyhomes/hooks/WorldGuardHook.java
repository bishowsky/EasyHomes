package com.easyhomes.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * WorldGuard integration for region permission checks
 * Prevents setting homes in regions where player can't build
 */
public class WorldGuardHook {
    private final Plugin plugin;
    private boolean enabled;

    public WorldGuardHook(Plugin plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }

    /**
     * Initialize WorldGuard hook
     */
    public boolean initialize() {
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            plugin.getLogger().info("WorldGuard not found - region checks disabled");
            return false;
        }

        enabled = true;
        plugin.getLogger().info("WorldGuard hooked successfully!");
        return true;
    }

    /**
     * Check if WorldGuard is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Check if player can build at location
     */
    public boolean canBuild(Player player, Location location) {
        if (!isEnabled()) {
            return true; // Allow if WorldGuard not available
        }

        try {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
            
            return query.testState(wgLocation, WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check WorldGuard permissions: " + e.getMessage());
            return true; // Allow on error
        }
    }

    /**
     * Check if location is in a protected region
     */
    public boolean isProtected(Location location) {
        if (!isEnabled()) {
            return false;
        }

        try {
            com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
            return WorldGuard.getInstance().getPlatform().getRegionContainer()
                    .createQuery()
                    .getApplicableRegions(wgLocation)
                    .size() > 0;
                    
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check WorldGuard region: " + e.getMessage());
            return false;
        }
    }
}
