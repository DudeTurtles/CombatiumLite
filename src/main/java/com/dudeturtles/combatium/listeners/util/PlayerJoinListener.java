package com.dudeturtles.combatium.listeners.util;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.util.hitThroughTransparent.HitManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.Objects;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.ENTITY_INTERACTION_RANGE)).setBaseValue(2.5);
        Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.ATTACK_SPEED)).setBaseValue(4.0);

        CombatiumLite.getReachUtil().resetCooldown(event.getPlayer(), false);

        if (CombatiumLite.hitThroughTransparent) HitManager.updatePlayerData(event.getPlayer());
        event.getPlayer().setShieldBlockingDelay(0);
        event.getPlayer().setMaximumNoDamageTicks(0);

        Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(2.0);
    }
}
