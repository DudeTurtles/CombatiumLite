package com.combatium.listeners.util;

import com.combatium.CombatiumLite;
import com.combatium.util.hitThroughTransparent.HitManager;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.combatium.util.active.ReachUtil;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import static com.combatium.listeners.snapshot.CombatifyListener.isCombatifyPlayer;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.ENTITY_INTERACTION_RANGE)).setBaseValue(2.5);
        if (!isCombatifyPlayer(event.getPlayer())) Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.ATTACK_SPEED)).setBaseValue(1.25);
        else Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.ATTACK_SPEED)).setBaseValue(4.0);

        ReachUtil.resetCooldown(event.getPlayer(), false, new ItemStack(Material.AIR));

        if (CombatiumLite.hitThroughTransparent) HitManager.updatePlayerData(event.getPlayer());
        event.getPlayer().setShieldBlockingDelay(0);
        event.getPlayer().setMaximumNoDamageTicks(0);

        Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(2.0);
    }
}
