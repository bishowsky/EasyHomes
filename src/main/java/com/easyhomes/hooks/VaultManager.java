package com.easyhomes.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

/**
 * Manages Vault economy integration
 * Handles payment for teleportation and home creation
 */
public class VaultManager {
    private final Plugin plugin;
    private Economy economy;
    private boolean enabled;

    public VaultManager(Plugin plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }

    /**
     * Initialize Vault economy hook
     */
    public boolean initialize() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault not found - economy features disabled");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        
        if (rsp == null) {
            plugin.getLogger().warning("No economy plugin found - economy features disabled");
            return false;
        }

        economy = rsp.getProvider();
        enabled = true;
        plugin.getLogger().info("Vault economy hooked successfully!");
        return true;
    }

    /**
     * Check if economy is enabled
     */
    public boolean isEnabled() {
        return enabled && economy != null;
    }

    /**
     * Check if player has enough money
     */
    public boolean has(Player player, double amount) {
        if (!isEnabled()) {
            return true;
        }

        return economy.has(player, amount);
    }

    /**
     * Withdraw money from player
     */
    public boolean withdraw(Player player, double amount) {
        if (!isEnabled()) {
            return true;
        }

        if (!has(player, amount)) {
            return false;
        }

        try {
            economy.withdrawPlayer(player, amount);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to withdraw money from " + player.getName(), e);
            return false;
        }
    }

    /**
     * Deposit money to player
     */
    public boolean deposit(Player player, double amount) {
        if (!isEnabled()) {
            return true;
        }

        try {
            economy.depositPlayer(player, amount);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to deposit money to " + player.getName(), e);
            return false;
        }
    }

    /**
     * Get player balance
     */
    public double getBalance(Player player) {
        if (!isEnabled()) {
            return 0.0;
        }

        return economy.getBalance(player);
    }

    /**
     * Format money amount
     */
    public String format(double amount) {
        if (!isEnabled()) {
            return String.format("%.2f", amount);
        }

        return economy.format(amount);
    }

    /**
     * Get currency name
     */
    public String getCurrencyName(int amount) {
        if (!isEnabled()) {
            return "monet";
        }

        if (amount == 1) {
            return economy.currencyNameSingular();
        } else {
            return economy.currencyNamePlural();
        }
    }
}
