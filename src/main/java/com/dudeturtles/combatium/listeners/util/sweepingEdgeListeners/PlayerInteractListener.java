package com.dudeturtles.combatium.listeners.util.sweepingEdgeListeners;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.listeners.snapshot.CombatifyListener;
import com.dudeturtles.combatium.listeners.util.AnimationListener;
import com.dudeturtles.combatium.util.active.ReachUtil;
import com.dudeturtles.combatium.util.active.SweepingEdgeUtil;
import com.dudeturtles.combatium.util.hitThroughTransparent.HitManager;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
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
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_AIR && isPotentialFalseInput(player, 0)) {
            return;
        }
    
        if (CombatiumLite.hitThroughTransparent && event.getAction() == Action.LEFT_CLICK_BLOCK && Objects.requireNonNull(event.getClickedBlock()).isPassable() && !CombatifyListener.isCombatifyPlayer(player)) {
            LivingEntity entity = HitManager.checkHit(player);
            if (entity != null) {
                event.setCancelled(true);
                if (!reachUtil.isRateLimited(player)) player.attack(entity);
                return;
            }
        }
    
        if (event.getAction().isLeftClick()) {
            if (reachUtil.isCharged(player, 0.0F)) {
                if (CombatiumLite.fixSweepOnThrow) {
                    CombatiumLite.getScheduler().scheduleSweep(player);
                    AnimationListener.cancelAnim(player);
                    return;
                } else {
                    if (CombatiumLite.sweepOnMiss && event.getAction() == Action.LEFT_CLICK_AIR && reachUtil.isOverCharged(player, 1.0F) &&
                            SweepingEdgeUtil.hasSweeping(player)) {
                        SweepingEdgeUtil.sweepAttack(player, null);
                    }
                    CombatiumLite.getReachUtil().resetCooldown(player, true);
                    net.minecraft.world.entity.LivingEntity nmsAttacker = ((CraftLivingEntity) player).getHandle();
                    nmsAttacker.lungeForwardMaybe();
                    nmsAttacker.swing(nmsAttacker.swingingArm, true);
                }
    
                if (!(reachUtil.isRateLimited(player))) {
                    reachUtil.resetCooldown(player, true);
                } else {
                    AnimationListener.cancelAnim(player);
                }
            } else if (event.getAction() == Action.LEFT_CLICK_AIR) {
                AnimationListener.cancelAnim(player);
            }
        }
    
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) setLastPotentialFalseInput(player);
    }
    
    public static void setLastPotentialFalseInput(Player player) {
        lastPotentialFalseInput.put(player, CombatiumLite.getScheduler().getCurrentTick());
    }
    
    public static boolean isPotentialFalseInput(Player player, int offset) {
        return lastPotentialFalseInput.containsKey(player) && Math.abs(lastPotentialFalseInput.get(player) - CombatiumLite.getScheduler().getCurrentTick()) <= offset;
    }
}
