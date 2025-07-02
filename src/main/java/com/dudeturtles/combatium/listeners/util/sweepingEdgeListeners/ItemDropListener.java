package com.dudeturtles.combatium.listeners.util.sweepingEdgeListeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        PlayerInteractListener.setLastPotentialFalseInput(event.getPlayer());
    }
}
