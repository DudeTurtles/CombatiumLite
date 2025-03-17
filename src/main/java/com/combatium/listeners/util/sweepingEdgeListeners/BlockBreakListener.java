package com.combatium.listeners.util.sweepingEdgeListeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        PlayerInteractListener.setLastPotentialFalseInput(event.getPlayer());
    }
}
