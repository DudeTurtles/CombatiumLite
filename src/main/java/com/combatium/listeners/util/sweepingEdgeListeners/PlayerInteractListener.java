package com.combatium.listeners.util.sweepingEdgeListeners;

import com.combatium.CombatiumLite;
import com.combatium.listeners.util.AnimationListener;
import com.combatium.scheduler.Scheduler;
import com.combatium.util.active.ReachUtil;
import com.combatium.util.active.SweepingEdgeUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.combatium.CombatiumLite.fixSweepOnThrow;
import static com.combatium.listeners.snapshot.CombatifyListener.isCombatifyPlayer;
import static com.combatium.scheduler.Scheduler.scheduleSweep;
import static com.combatium.util.active.ReachUtil.isRateLimited;
import static com.combatium.util.hitThroughTransparent.HitManager.checkHit;
import static org.bukkit.event.block.Action.*;

public class PlayerInteractListener implements Listener {
    private static final Map<Player, Integer> lastPotentialFalseInput = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == LEFT_CLICK_AIR && isPotentialFalseInput(event.getPlayer(), 0)) {
            return;
        }

        if (CombatiumLite.hitThroughTransparent && event.getAction() == LEFT_CLICK_BLOCK && Objects.requireNonNull(event.getClickedBlock()).isPassable() && !isCombatifyPlayer(event.getPlayer())) {
            LivingEntity entity = checkHit(event.getPlayer());
            if (entity != null) {
                event.setCancelled(true);
                if (!isRateLimited(event.getPlayer())) event.getPlayer().attack(entity);
                return;
            }
        }

        if (event.getAction().isLeftClick()) {
            if (ReachUtil.isCharged(event.getPlayer())) {
                if (CombatiumLite.sweepOnMiss && event.getAction() == LEFT_CLICK_AIR && ReachUtil.isOverCharged(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand()) &&
                        (SweepingEdgeUtil.getSweepingLevel(event.getPlayer()) > 0)) {
                    if (fixSweepOnThrow) {
                        scheduleSweep(event.getPlayer());
                        AnimationListener.cancelAnim(event.getPlayer());
                        return;
                    } else SweepingEdgeUtil.Sweep(event.getPlayer(), null);
                }

                if (!(isRateLimited(event.getPlayer()))) {
                    ReachUtil.resetCooldown(event.getPlayer(), true, event.getPlayer().getInventory().getItemInMainHand());
                } else {
                    AnimationListener.cancelAnim(event.getPlayer());
                }
            } else if (event.getAction() == LEFT_CLICK_AIR) {
                AnimationListener.cancelAnim(event.getPlayer());
            }
        }

        if (event.getAction() == RIGHT_CLICK_BLOCK) setLastPotentialFalseInput(event.getPlayer());
    }

    public static void setLastPotentialFalseInput(Player player) {
        lastPotentialFalseInput.put(player, Scheduler.getCurrentTick());
    }

    public static boolean isPotentialFalseInput(Player player, int offset) {
        return lastPotentialFalseInput.containsKey(player) && Math.abs(lastPotentialFalseInput.get(player) - Scheduler.getCurrentTick()) <= offset;
    }
}
