package com.easyhomes;

import com.easyhomes.commands.DelHomeCommand;
import com.easyhomes.commands.EasyHomesCommand;
import com.easyhomes.commands.HomeCommand;
import com.easyhomes.commands.SetHomeCommand;
import com.easyhomes.listeners.CombatListener;
import com.easyhomes.manager.CombatManager;
import com.easyhomes.manager.CooldownManager;
import com.easyhomes.manager.HomeManager;
import com.easyhomes.manager.TeleportManager;
import com.easyhomes.storage.HomeStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class EasyHomes extends JavaPlugin implements Listener {
    private HomeStorage homeStorage;
    private HomeManager homeManager;
    private CooldownManager cooldownManager;
    private CombatManager combatManager;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize storage
        homeStorage = new HomeStorage(this);

        // Initialize managers
        homeManager = new HomeManager(homeStorage, getConfig());

        // Initialize cooldown manager
        int defaultCooldown = getConfig().getInt("cooldowns.default", 60);
        Map<String, Integer> groupCooldowns = new HashMap<>();
        if (getConfig().isConfigurationSection("cooldowns.groups")) {
            for (String group : getConfig().getConfigurationSection("cooldowns.groups").getKeys(false)) {
                groupCooldowns.put(group, getConfig().getInt("cooldowns.groups." + group));
            }
        }
        cooldownManager = new CooldownManager(defaultCooldown, groupCooldowns);

        // Initialize combat manager
        boolean combatEnabled = getConfig().getBoolean("combat.enabled", true);
        int combatDuration = getConfig().getInt("combat.duration", 10);
        combatManager = new CombatManager(combatEnabled, combatDuration);

        // Initialize teleport manager
        int teleportDelay = getConfig().getInt("teleport.delay", 3);
        boolean cancelOnMove = getConfig().getBoolean("teleport.cancel-on-move", true);
        boolean particlesEnabled = getConfig().getBoolean("teleport.particles.enabled", true);
        String particleType = getConfig().getString("teleport.particles.type", "PORTAL");
        int particleAmount = getConfig().getInt("teleport.particles.amount", 50);
        boolean showAtStart = getConfig().getBoolean("teleport.particles.show-at-start", true);
        boolean showAtDestination = getConfig().getBoolean("teleport.particles.show-at-destination", true);
        boolean soundsEnabled = getConfig().getBoolean("teleport.sounds.enabled", true);
        String soundType = getConfig().getString("teleport.sounds.type", "ENDERMAN_TELEPORT");
        float soundVolume = (float) getConfig().getDouble("teleport.sounds.volume", 1.0);
        float soundPitch = (float) getConfig().getDouble("teleport.sounds.pitch", 1.0);
        boolean titleEnabled = getConfig().getBoolean("teleport.title.enabled", true);
        String titleText = getConfig().getString("teleport.title.title", "&aTeleporting...");
        String subtitleText = getConfig().getString("teleport.title.subtitle", "&e{time} seconds remaining");

        teleportManager = new TeleportManager(
                this, teleportDelay, cancelOnMove,
                particlesEnabled, particleType, particleAmount, showAtStart, showAtDestination,
                soundsEnabled, soundType, soundVolume, soundPitch,
                titleEnabled, titleText, subtitleText);

        // Register commands
        HomeCommand homeCommand = new HomeCommand(homeManager, cooldownManager, combatManager, teleportManager, getConfig());
        getCommand("home").setExecutor(homeCommand);
        getCommand("home").setTabCompleter(homeCommand);

        SetHomeCommand setHomeCommand = new SetHomeCommand(homeManager, getConfig());
        getCommand("sethome").setExecutor(setHomeCommand);
        getCommand("sethome").setTabCompleter(setHomeCommand);

        DelHomeCommand delHomeCommand = new DelHomeCommand(homeManager, getConfig());
        getCommand("delhome").setExecutor(delHomeCommand);
        getCommand("delhome").setTabCompleter(delHomeCommand);

        // Register admin command
        getCommand("easyhomes").setExecutor(new EasyHomesCommand(this, getConfig()));

        // Register listeners
        getServer().getPluginManager().registerEvents(new CombatListener(combatManager), this);
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("EasyHomes has been enabled!");
    }

    @Override
    public void onDisable() {
        // Cancel all pending teleports
        if (teleportManager != null) {
            teleportManager.clearAll();
        }

        // Clear all combat tags
        if (combatManager != null) {
            combatManager.clearAll();
        }

        // Clear all cooldowns
        if (cooldownManager != null) {
            cooldownManager.clearAll();
        }

        getLogger().info("EasyHomes has been disabled!");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Cancel any pending teleport
        if (teleportManager != null) {
            teleportManager.cancelTeleport(player);
        }

        // Unload player data
        if (homeManager != null) {
            homeManager.unloadPlayer(player.getUniqueId());
        }
    }
}
