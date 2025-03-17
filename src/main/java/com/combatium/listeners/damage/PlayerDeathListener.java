package com.combatium.listeners.damage;

import com.combatium.scheduler.Scheduler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDeathListener implements Listener {
    private static final Map<UUID, Integer> lastDeath = new HashMap<>();
    private static final Map<UUID, Boolean> cancelled = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathStart(PlayerDeathEvent event) {
        if (lastDeath.containsKey(event.getPlayer().getUniqueId()) && Math.abs(Scheduler.getCurrentTick() - lastDeath.get(event.getEntity().getUniqueId())) <= 1) {
            cancelled.put(event.getPlayer().getUniqueId(), true);
            event.setCancelled(true);
            event.deathMessage(null);
        }
        lastDeath.put(event.getPlayer().getUniqueId(), Scheduler.getCurrentTick());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeathEnd(PlayerDeathEvent event) {
        if (cancelled.containsKey(event.getPlayer().getUniqueId()) && cancelled.get(event.getPlayer().getUniqueId())) {
            cancelled.remove(event.getPlayer().getUniqueId());
            event.setCancelled(false);
            event.deathMessage(null);
        }
    }
}
