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
    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityDamageByEntity(EntityDamageByEntityEvent event) {
        /*
         * Maintain killing hits to ensure death messages are not modified
         * Magic damage is only changed when dealing with arrows, which is handled elsewhere
         * Ensure event is cancellable
         */
        if (event.getDamageSource().getDamageType() == DamageType.GENERIC_KILL || event.getDamageSource().getDamageType() == DamageType.INDIRECT_MAGIC || event.getDamageSource().getDamageType() == DamageType.THORNS || event.isCancelled()) {
            return;
        }

        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof LivingEntity victim) {
            if (hasProcessed(attacker)) {
                if (victim instanceof Player) {
                    if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < 0) {
                        event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, 0);
                        event.setDamage(getFinalDamage(victim, (float) event.getDamage(), attacker.getInventory().getItemInMainHand()));
                        if (victim.getAbsorptionAmount() > 0) {
                            if (victim.getAbsorptionAmount() > event.getDamage()) {
                                event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, event.getDamage() * (-1));
                                victim.setAbsorptionAmount(victim.getAbsorptionAmount() - event.getDamage());
                            } else {
                                event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, victim.getAbsorptionAmount() * (-1));
                                victim.setAbsorptionAmount(0);
                            }
                        }
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
            if (ReachUtil.isCharged(attacker)) {
                PlayerDamageEntity.playerDamageEntityEvent(attacker, victim, weapon, event.getCause(), true);
            } else {
                if (weapon.getType() != Material.MACE && CombatiumLite.gracePeriod && !isCombatifyPlayer(attacker)) {
                    //Grace period for early hits
                    int earlyTicks = ReachUtil.earlyHit(attacker);
                    if (earlyTicks > 0) {
                        ScheduledHit scheduledHit = new ScheduledHit(attacker, victim, EntityDamageEvent.DamageCause.ENTITY_ATTACK, earlyTicks);
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
                    ticks = ReachUtil.getCooldown(de.getActiveItem(), 0);
                } else {
                    ticks = 10;
                }

                InvulnerabilityScheduler.setInvulnerable(victim, ticks, originalDamage);
            }
        }
    }
}
