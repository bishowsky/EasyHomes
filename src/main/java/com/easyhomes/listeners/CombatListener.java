package com.easyhomes.listeners;

import com.easyhomes.manager.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {
    private final CombatManager combatManager;

    public CombatListener(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!combatManager.isEnabled()) {
            return;
        }

        Player victim = null;
        Player attacker = null;

        // Check if victim is a player
        if (event.getEntity() instanceof Player) {
            victim = (Player) event.getEntity();
        }

        // Check if attacker is a player or projectile shot by player
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        }

        // Tag both players if this is PvP
        if (victim != null && attacker != null) {
            combatManager.tagPlayer(victim);
            combatManager.tagPlayer(attacker);
        }
    }
}
