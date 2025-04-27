package com.combatium.listeners.util;

import com.combatium.CombatiumLite;
import com.combatium.util.hitThroughTransparent.HitManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class DisconnectListener implements Listener {
    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        HitManager.removePlayerData(event.getPlayer());
        CombatiumLite.getReachUtil().removePlayer(event.getPlayer());
    }
}
