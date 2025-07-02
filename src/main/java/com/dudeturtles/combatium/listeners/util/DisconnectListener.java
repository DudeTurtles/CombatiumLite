package com.dudeturtles.combatium.listeners.util;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.util.hitThroughTransparent.HitManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class DisconnectListener implements Listener {
    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        HitManager.removePlayerData(event.getPlayer());
        CombatiumLite.getReachUtil().removePlayer(event.getPlayer());


        Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(1.0);
        Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.ENTITY_INTERACTION_RANGE)).setBaseValue(3);
        Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.ATTACK_SPEED)).setBaseValue(4.0);
        Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.ENTITY_INTERACTION_RANGE)).removeModifier(CombatiumLite.getReachUtil().chargedReach);
    }
}
