package com.combatium.listeners.damage;

import com.combatium.CombatiumLite;
import com.combatium.listeners.util.AnimationListener;
import com.combatium.scheduler.InvulnerabilityScheduler;
import com.combatium.scheduler.ScheduledHit;
import com.combatium.scheduler.Scheduler;
import com.combatium.util.active.PlayerDamageEntity;
import com.combatium.util.active.ReachUtil;
import org.bukkit.Material;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import static com.combatium.listeners.snapshot.CombatifyListener.isCombatifyPlayer;
import static com.combatium.util.active.PlayerDamageEntity.hasProcessed;
import static com.combatium.util.calculations.PlayerHitUtil.getFinalDamage;

public class EntityDamageByEntityListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityDamageByEntity(EntityDamageByEntityEvent event) {
        /*
         * Maintain killing hits to ensure death messages are not modified
         * Magic damage is only changed when dealing with arrows, which is handled elsewhere
         * Ensure event is cancellable
         */
        DamageType damageType = event.getDamageSource().getDamageType();
        if (damageType == DamageType.GENERIC_KILL || damageType == DamageType.INDIRECT_MAGIC || damageType == DamageType.THORNS || event.isCancelled()) {
            return;
        }

        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof LivingEntity victim) {
            if (hasProcessed(attacker)) {
                if (victim instanceof Player && (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < 0)) {
                    event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, 0);
                    event.setDamage(getFinalDamage(victim, (float) event.getDamage(), attacker.getInventory().getItemInMainHand()));
                    if (victim.getAbsorptionAmount() > 0) {
                        event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, Math.min(victim.getAbsorptionAmount(), event.getDamage()));
                        victim.setAbsorptionAmount(Math.max(0, victim.getAbsorptionAmount() - event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION)));
                    }
                }
                hasProcessed.put(attacker, false);
                return;
            }

            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            if (ReachUtil.isRateLimited(attacker) && (event.getCause() != EntityDamageByEntityEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
                AnimationListener.cancelAnim(attacker);
                event.setCancelled(true);
                return;
            }

            if (!weapon.containsEnchantment(Enchantment.SWEEPING_EDGE) && event.getCause() == EntityDamageByEntityEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                event.setCancelled(true);
                return;
            }

            //Checks if a player is charged, also allows sweep damage to be dealt due to sweep damage being a separate event
            if (ReachUtil.isCharged(attacker, 0.0F)) {
                PlayerDamageEntity.playerDamageEntityEvent(attacker, victim, weapon, event.getCause(), true);
            } else {
                if (weapon.getType() != Material.MACE && CombatiumLite.gracePeriod && !isCombatifyPlayer(attacker)) {
                    //Grace period for early hits
                    if (ReachUtil.getChargePercent(attacker, 0.0F) < 1.0F && ReachUtil.getChargePercent(attacker, 1.0F) > 0.8F) {
                        ScheduledHit scheduledHit = new ScheduledHit(attacker, victim, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
                        Scheduler.scheduleHit(scheduledHit);
                    }
                }
                AnimationListener.cancelAnim(attacker);
            }
            event.setCancelled(true);
            ReachUtil.rateLimit(attacker);
            victim.setNoDamageTicks(0);
        } else if (event.getDamager() instanceof Trident trident) { // Updated trident damage calculation
            double damage = 8;
            if (trident.getItemStack().containsEnchantment(Enchantment.IMPALING) && (event.getEntity()).isInWaterOrRain()) {
                damage += trident.getItemStack().getEnchantmentLevel(Enchantment.IMPALING) * 2.5;
            }
            event.setDamage(damage);
        } else if (event.getEntity() instanceof LivingEntity victim) { // Ensure replacement i-frames work for other cases
            double invulDamage = InvulnerabilityScheduler.isInvulnerable(victim);

            if (invulDamage >= event.getDamage()) {
                event.setCancelled(true);
                event.setDamage(0);
                return;
            }

            double originalDamage = event.getDamage();
            event.setDamage(event.getDamage() - invulDamage);

            if (!(event.getDamager() instanceof Projectile)) { // Ensure i-frames are set when damage is not dealt by a projectile
                int ticks;
                if (event.getDamager() instanceof LivingEntity de && de.getActiveItem().getType() != Material.AIR) {
                    ticks = ReachUtil.getCooldown(de);
                } else {
                    ticks = 10;
                }

                InvulnerabilityScheduler.setInvulnerable(victim, ticks, originalDamage, event.getCause());
            }
        }
    }
}
