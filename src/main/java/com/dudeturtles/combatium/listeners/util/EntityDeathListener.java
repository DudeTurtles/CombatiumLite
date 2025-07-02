package com.dudeturtles.combatium.listeners.util;

import com.dudeturtles.combatium.CombatiumLite;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        CombatiumLite.getScheduler().getInvulnerabilityScheduler().remove(event.getEntity());
    }
}
