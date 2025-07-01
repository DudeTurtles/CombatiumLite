package com.combatium.listeners.damage;

import com.combatium.CombatiumLite;
import com.combatium.listeners.snapshot.CombatifyListener;
import com.combatium.listeners.util.AnimationListener;
import com.combatium.scheduler.ScheduledHit;
import com.combatium.scheduler.Scheduler;
import com.combatium.util.active.PlayerDamageEntity;
import com.combatium.util.active.ReachUtil;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class PrePlayerAttackEvent implements Listener {
    private final Scheduler scheduler;
    private final ReachUtil reachUtil;

    public PrePlayerAttackEvent() {
        this.scheduler = CombatiumLite.getScheduler();
        this.reachUtil = CombatiumLite.getReachUtil();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void prePlayerAttack(PrePlayerAttackEntityEvent event) {
        Player attacker = event.getPlayer();

        if (!(event.getAttacked() instanceof LivingEntity victim)) {
            if (reachUtil.isCharged(attacker, 0.0F)) {
                CombatiumLite.getReachUtil().resetCooldown(attacker, false);
            } else {
                AnimationListener.cancelAnim(attacker);
                event.setCancelled(true);
            }
            reachUtil.rateLimit(attacker);
            return;
        }

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (reachUtil.isRateLimited(attacker)) {
            AnimationListener.cancelAnim(attacker);
            event.setCancelled(true);
            return;
        }

        //Checks if a player is charged, also allows sweep damage to be dealt due to sweep damage being a separate event
        if (reachUtil.isCharged(attacker, 0.0F)) {
            PlayerDamageEntity.playerDamageEntityEvent(attacker, victim, weapon, EntityDamageEvent.DamageCause.ENTITY_ATTACK, true);
        } else {
            if (weapon.getType() != Material.MACE && CombatiumLite.gracePeriod && !CombatifyListener.isCombatifyPlayer(attacker)) {
                //Grace period for early hits
                if (reachUtil.getChargePercent(attacker, 0.0F) < 1.0F && reachUtil.getChargePercent(attacker, 1.0F) > 0.8F) {
                    ScheduledHit scheduledHit = new ScheduledHit(attacker, victim, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
                    scheduler.scheduleHit(scheduledHit);
                }
            }
            AnimationListener.cancelAnim(attacker);
        }
        event.setCancelled(true);
        reachUtil.rateLimit(attacker);
        victim.setNoDamageTicks(0);
    }
}
