package com.dudeturtles.combatium.listeners.util.sweepingEdgeListeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

public class BlockBreakListener implements Listener {
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        PlayerInteractListener.setLastPotentialFalseInput(event.getPlayer());
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        PlayerInteractListener.setLastPotentialFalseInput(event.getPlayer());
    }
}
