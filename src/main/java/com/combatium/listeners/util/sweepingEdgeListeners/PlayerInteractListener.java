package com.combatium.listeners.util.sweepingEdgeListeners;

import com.combatium.CombatiumLite;
import com.combatium.listeners.snapshot.CombatifyListener;
import com.combatium.listeners.util.AnimationListener;
import com.combatium.util.active.ReachUtil;
import com.combatium.util.active.SweepingEdgeUtil;
import com.combatium.util.hitThroughTransparent.HitManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerInteractListener implements Listener {
    private static final Map<Player, Integer> lastPotentialFalseInput = new HashMap<>();
    private final ReachUtil reachUtil;

public PlayerInteractListener() {
        this.reachUtil = CombatiumLite.getReachUtil();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR && isPotentialFalseInput(event.getPlayer(), 0)) {
            return;
        }

        if (CombatiumLite.hitThroughTransparent && event.getAction() == Action.LEFT_CLICK_BLOCK && Objects.requireNonNull(event.getClickedBlock()).isPassable() && !CombatifyListener.isCombatifyPlayer(event.getPlayer())) {
            LivingEntity entity = HitManager.checkHit(event.getPlayer());
            if (entity != null) {
                event.setCancelled(true);
                if (!reachUtil.isRateLimited(event.getPlayer())) event.getPlayer().attack(entity);
                return;
            }
        }

        if (event.getAction().isLeftClick()) {
            if (reachUtil.isCharged(event.getPlayer(), 0.0F)) {
                if (CombatiumLite.sweepOnMiss && event.getAction() == Action.LEFT_CLICK_AIR && reachUtil.isOverCharged(event.getPlayer(), 0.0F) &&
                        (SweepingEdgeUtil.getSweepingLevel(event.getPlayer()) > 0)) {
                    if (CombatiumLite.fixSweepOnThrow) {
                        CombatiumLite.getScheduler().scheduleSweep(event.getPlayer());
                        AnimationListener.cancelAnim(event.getPlayer());
                        return;
                    } else {
                        SweepingEdgeUtil.Sweep(event.getPlayer(), null);
                    }
                }

                if (!(reachUtil.isRateLimited(event.getPlayer()))) {
                    reachUtil.resetCooldown(event.getPlayer(), true);
                } else {
                    AnimationListener.cancelAnim(event.getPlayer());
                }
            } else if (event.getAction() == Action.LEFT_CLICK_AIR) {
                AnimationListener.cancelAnim(event.getPlayer());
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) setLastPotentialFalseInput(event.getPlayer());
    }

    public static void setLastPotentialFalseInput(Player player) {
        lastPotentialFalseInput.put(player, CombatiumLite.getScheduler().getCurrentTick());
    }

    public static boolean isPotentialFalseInput(Player player, int offset) {
        return lastPotentialFalseInput.containsKey(player) && Math.abs(lastPotentialFalseInput.get(player) - CombatiumLite.getScheduler().getCurrentTick()) <= offset;
    }
}
