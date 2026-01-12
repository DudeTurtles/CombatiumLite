package com.dudeturtles.combatium.listeners.damage;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.listeners.snapshot.CombatifyListener;
import com.dudeturtles.combatium.listeners.util.AnimationListener;
import com.dudeturtles.combatium.scheduler.ScheduledHit;
import com.dudeturtles.combatium.scheduler.Scheduler;
import com.dudeturtles.combatium.util.active.PlayerDamageEntity;
import com.dudeturtles.combatium.util.active.ReachUtil;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
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


        net.minecraft.world.entity.player.Player nmsAttacker = ((CraftPlayer) attacker).getHandle();
        net.minecraft.world.item.ItemStack nmsWeapon = nmsAttacker.getActiveItem();
        boolean isUsingKineticWeapon = nmsWeapon.has(DataComponents.KINETIC_WEAPON) && nmsAttacker.isUsingItem() && nmsWeapon == nmsAttacker.getUseItem();
        boolean isUsingPiercingWeapon = nmsWeapon.has(DataComponents.PIERCING_WEAPON) && !nmsAttacker.isUsingItem() && nmsWeapon == nmsAttacker.getMainHandItem();
        if (isUsingKineticWeapon || isUsingPiercingWeapon)
            return;
        if (!(event.getAttacked() instanceof LivingEntity victim)) {
            if (reachUtil.isCharged(attacker, 0.0F)) {
                CombatiumLite.getReachUtil().resetCooldown(attacker, false);
            } else {
                PlayerDamageEntity.playServerSideSound(((CraftPlayer) attacker).getHandle(), SoundEvents.PLAYER_ATTACK_NODAMAGE);
                AnimationListener.cancelAnim(attacker);
                event.setCancelled(true);
            }
            reachUtil.rateLimit(attacker);
            return;
        }
        ItemStack weapon = nmsWeapon.asBukkitMirror();
        if (reachUtil.isRateLimited(attacker)) {
            AnimationListener.cancelAnim(attacker);
            event.setCancelled(true);
            return;
        }

        //Checks if a player is charged, also allows sweep damage to be dealt due to sweep damage being a separate event
        if (reachUtil.isCharged(attacker, 0.0F)) {
            if (PlayerDamageEntity.playerDamageEntityEvent(attacker, victim, weapon, EntityDamageEvent.DamageCause.ENTITY_ATTACK, true))
                PlayerDamageEntity.playServerSideSound(((CraftPlayer) attacker).getHandle(), SoundEvents.PLAYER_ATTACK_NODAMAGE);
        } else {
            if (weapon.getType() != Material.MACE && CombatiumLite.gracePeriod && !CombatifyListener.isCombatifyPlayer(attacker)) {
                //Grace period for early hits
                if (reachUtil.getChargePercent(attacker, 0.0F) < 1.0F && reachUtil.getChargePercent(attacker, 1.0F) > 0.8F) {
                    ScheduledHit scheduledHit = new ScheduledHit(attacker, victim, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
                    scheduler.scheduleHit(scheduledHit);
                } else
                    PlayerDamageEntity.playServerSideSound(((CraftPlayer) attacker).getHandle(), SoundEvents.PLAYER_ATTACK_NODAMAGE);
            }
            AnimationListener.cancelAnim(attacker);
        }
        event.setCancelled(true);
        reachUtil.rateLimit(attacker);
        victim.setNoDamageTicks(0);
    }
}
