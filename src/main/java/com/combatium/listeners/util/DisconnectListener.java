package com.combatium.listeners.util;

import com.combatium.util.active.ReachUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class DisconnectListener implements Listener {
    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        ReachUtil.removePlayer(event.getPlayer());
    }
}
