package com.dudeturtles.combatium.listeners.projectile;

import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.BlockProjectileSource;

public class TridentHitListener implements Listener {
    @EventHandler
    public void onTridentHit(ProjectileHitEvent event) { //Ensures tridents can be picked up after hitting an entity
        if (event.getEntity() instanceof Trident trident && trident.getShooter() instanceof BlockProjectileSource) {
            trident.setPickupStatus(Trident.PickupStatus.ALLOWED);
        }
    }
}
