package com.easyhomes;

import com.easyhomes.commands.DelHomeCommand;
import com.easyhomes.commands.EasyHomesCommand;
import com.easyhomes.commands.HomeCommand;
import com.easyhomes.commands.SetHomeCommand;
import com.easyhomes.database.DatabaseManager;
import com.easyhomes.hooks.EasyHomesExpansion;
import com.easyhomes.hooks.VaultManager;
import com.easyhomes.hooks.WorldGuardHook;
import com.easyhomes.listeners.CombatListener;
import com.easyhomes.manager.CombatManager;
import com.easyhomes.manager.CooldownManager;
import com.easyhomes.manager.HomeManager;
import com.easyhomes.manager.TeleportManager;
import com.easyhomes.storage.HomeStorage;
import com.easyhomes.util.DebugManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * EasyHomes - Profesjonalny system zarządzania domami
 * Autor: Bishyy
 * Discord Support: https://discord.gg/mkyU3SgBUP
 */
public class EasyHomes extends JavaPlugin implements Listener {
    // Core components
    private DatabaseManager databaseManager;
    private HomeStorage homeStorage;
    private HomeManager homeManager;
    private CooldownManager cooldownManager;
    private CombatManager combatManager;
    private TeleportManager teleportManager;
    private DebugManager debugManager;
    
    // Hooks
    private VaultManager vaultManager;
    private WorldGuardHook worldGuardHook;
    private EasyHomesExpansion placeholderExpansion;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        
        // Save default config
        saveDefaultConfig();
        
        getLogger().info("=================================");
        getLogger().info("   EasyHomes - Ładowanie...");
        getLogger().info("   Autor: Bishyy");
        getLogger().info("   Discord: discord.gg/mkyU3SgBUP");
        getLogger().info("=================================");

        // Initialize debug manager first
        debugManager = new DebugManager(this);
        getLogger().info("✓ DebugManager zainicjalizowany");

        // Initialize database
        databaseManager = new DatabaseManager(this, getConfig());
        boolean databaseEnabled = databaseManager.initialize();
        
        if (databaseEnabled) {
            getLogger().info("✓ MySQL połączony pomyślnie!");
        } else {
            getLogger().warning("⚠ MySQL niedostępny - używam YAML fallback");
        }

        // Initialize storage with cache
        homeStorage = new HomeStorage(this, getConfig(), databaseManager);
        getLogger().info("✓ HomeStorage zainicjalizowany");

        // Initialize managers
        homeManager = new HomeManager(homeStorage, getConfig());
        getLogger().info("✓ HomeManager zainicjalizowany");

        // Initialize cooldown manager
        int defaultCooldown = getConfig().getInt("cooldowns.default", 60);
        Map<String, Integer> groupCooldowns = new HashMap<>();
        if (getConfig().isConfigurationSection("cooldowns.groups")) {
            for (String group : getConfig().getConfigurationSection("cooldowns.groups").getKeys(false)) {
                groupCooldowns.put(group, getConfig().getInt("cooldowns.groups." + group));
            }
        }
        cooldownManager = new CooldownManager(defaultCooldown, groupCooldowns);
        getLogger().info("✓ CooldownManager zainicjalizowany");

        // Initialize combat manager
        boolean combatEnabled = getConfig().getBoolean("combat.enabled", true);
        int combatDuration = getConfig().getInt("combat.duration", 10);
        combatManager = new CombatManager(combatEnabled, combatDuration);
        getLogger().info("✓ CombatManager zainicjalizowany");

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
        String titleText = getConfig().getString("teleport.title.title", "&aTeleportacja...");
        String subtitleText = getConfig().getString("teleport.title.subtitle", "&e{time} sekund pozostało");

        teleportManager = new TeleportManager(
                this, teleportDelay, cancelOnMove,
                particlesEnabled, particleType, particleAmount, showAtStart, showAtDestination,
                soundsEnabled, soundType, soundVolume, soundPitch,
                titleEnabled, titleText, subtitleText);
        getLogger().info("✓ TeleportManager zainicjalizowany");

        // Initialize hooks
        initializeHooks();

        // Register commands
        registerCommands();

        // Register listeners
        getServer().getPluginManager().registerEvents(new CombatListener(combatManager), this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("✓ Listenery zarejestrowane");

        long loadTime = System.currentTimeMillis() - startTime;
        getLogger().info("=================================");
        getLogger().info("  EasyHomes włączony! (" + loadTime + "ms)");
        getLogger().info("=================================");
    }

    /**
     * Initialize external plugin hooks
     */
    private void initializeHooks() {
        // Vault integration
        vaultManager = new VaultManager(this);
        if (getConfig().getBoolean("economy.enabled", false)) {
            if (vaultManager.initialize()) {
                getLogger().info("✓ Vault economy podłączony");
            } else {
                getLogger().warning("⚠ Vault economy niedostępny (ekonomia wyłączona)");
            }
        } else {
            getLogger().info("  Ekonomia wyłączona w konfiguracji");
        }

        // WorldGuard integration
        worldGuardHook = new WorldGuardHook(this);
        if (getConfig().getBoolean("worldguard.check-build-permission", true)) {
            if (worldGuardHook.initialize()) {
                getLogger().info("✓ WorldGuard podłączony");
            } else {
                getLogger().info("  WorldGuard niedostępny (sprawdzanie regionów wyłączone)");
            }
        }

        // PlaceholderAPI integration
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new EasyHomesExpansion(homeManager, cooldownManager, combatManager);
            if (placeholderExpansion.register()) {
                getLogger().info("✓ PlaceholderAPI zarejestrowany");
            } else {
                getLogger().warning("⚠ Nie udało się zarejestrować PlaceholderAPI");
            }
        } else {
            getLogger().info("  PlaceholderAPI niedostępny");
        }
    }

    /**
     * Register all commands
     */
    private void registerCommands() {
        HomeCommand homeCommand = new HomeCommand(homeManager, cooldownManager, combatManager, 
                teleportManager, vaultManager, debugManager, getConfig());
        getCommand("home").setExecutor(homeCommand);
        getCommand("home").setTabCompleter(homeCommand);

        SetHomeCommand setHomeCommand = new SetHomeCommand(homeManager, vaultManager, 
                worldGuardHook, debugManager, getConfig());
        getCommand("sethome").setExecutor(setHomeCommand);
        getCommand("sethome").setTabCompleter(setHomeCommand);

        DelHomeCommand delHomeCommand = new DelHomeCommand(homeManager, vaultManager, 
                debugManager, getConfig());
        getCommand("delhome").setExecutor(delHomeCommand);
        getCommand("delhome").setTabCompleter(delHomeCommand);

        EasyHomesCommand adminCommand = new EasyHomesCommand(this, getConfig(), debugManager);
        getCommand("easyhomes").setExecutor(adminCommand);
        getCommand("easyhomes").setTabCompleter(adminCommand);
        
        getLogger().info("✓ Komendy zarejestrowane");
    }

    @Override
    public void onDisable() {
        getLogger().info("=================================");
        getLogger().info("   EasyHomes - Wyłączanie...");
        getLogger().info("=================================");

        // Cancel all pending teleports
        if (teleportManager != null) {
            teleportManager.clearAll();
            getLogger().info("✓ Anulowano oczekujące teleportacje");
        }

        // Clear all combat tags
        if (combatManager != null) {
            combatManager.clearAll();
            getLogger().info("✓ Wyczyszczono combat tagi");
        }

        // Clear all cooldowns
        if (cooldownManager != null) {
            cooldownManager.clearAll();
            getLogger().info("✓ Wyczyszczono cooldowny");
        }

        // Unregister PlaceholderAPI
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
            getLogger().info("✓ PlaceholderAPI wyrejestrowany");
        }

        // Close database connection
        if (databaseManager != null && databaseManager.isEnabled()) {
            databaseManager.close();
            getLogger().info("✓ MySQL rozłączony");
        }

        getLogger().info("=================================");
        getLogger().info("  EasyHomes wyłączony!");
        getLogger().info("=================================");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Cancel any pending teleport
        if (teleportManager != null) {
            teleportManager.cancelTeleport(player);
        }

        // Unload player data from cache
        if (homeManager != null) {
            homeManager.unloadPlayer(player.getUniqueId());
        }
    }

    // Getters for other classes
    public HomeManager getHomeManager() {
        return homeManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public DebugManager getDebugManager() {
        return debugManager;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }
}
