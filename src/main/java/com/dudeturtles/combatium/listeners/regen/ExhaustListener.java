package com.dudeturtles.combatium.listeners.regen;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExhaustionEvent;

public class ExhaustListener implements Listener {
    @EventHandler
    public void onExhaust(EntityExhaustionEvent event) {
        if (event.getExhaustionReason() == EntityExhaustionEvent.ExhaustionReason.REGEN) event.setCancelled(true);
    }
}
