package com.combatium.listeners.util;

import com.combatium.scheduler.InvulnerabilityScheduler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        InvulnerabilityScheduler.remove(event.getEntity());
    }
}
