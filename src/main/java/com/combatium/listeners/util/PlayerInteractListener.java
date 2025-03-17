package com.combatium.listeners.util;

import com.combatium.CombatiumLite;
import com.combatium.util.active.ReachUtil;
import com.combatium.util.active.SweepingEdgeUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.Objects;

import static com.combatium.util.active.ReachUtil.isRateLimited;
import static com.combatium.util.hitThroughTransparent.HitManager.checkHit;
import static org.bukkit.event.block.Action.LEFT_CLICK_AIR;
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (CombatiumLite.hitThroughTransparent && event.getAction() == LEFT_CLICK_BLOCK && Objects.requireNonNull(event.getClickedBlock()).isPassable()) {
            LivingEntity entity = checkHit(event.getPlayer());
            if (entity != null) {
                event.setCancelled(true);
                if (!isRateLimited(event.getPlayer())) event.getPlayer().attack(entity);
                return;
            }
        }

        if (event.getAction().isLeftClick()) {
            if (ReachUtil.isCharged(event.getPlayer())) {
                if (CombatiumLite.sweepOnMiss && event.getAction() == LEFT_CLICK_AIR && ReachUtil.isOverCharged(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand().getType(), ReachUtil.getHaste(event.getPlayer())) &&
                        (SweepingEdgeUtil.getSweepingLevel(event.getPlayer()) > 0)) {
                    SweepingEdgeUtil.Sweep(event.getPlayer(), null);
                }

                if (!(isRateLimited(event.getPlayer()))) {
                    ReachUtil.resetCooldown(event.getPlayer(), true, event.getPlayer().getInventory().getItemInMainHand().getType());
                } else {
                    AnimationListener.cancelAnim(event.getPlayer());
                }
            } else if (event.getAction() == LEFT_CLICK_AIR) {
                AnimationListener.cancelAnim(event.getPlayer());
            }
        }
    }
}
