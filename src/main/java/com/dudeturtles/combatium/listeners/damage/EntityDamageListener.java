package com.dudeturtles.combatium.listeners.damage;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.scheduler.InvulnerabilityScheduler;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemUseAnimation;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;


public class EntityDamageListener implements Listener {
    private InvulnerabilityScheduler invulnerabilityScheduler;

    public EntityDamageListener() {
        this.invulnerabilityScheduler = CombatiumLite.getScheduler().getInvulnerabilityScheduler();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity e && event instanceof EntityDamageByEntityEvent && !event.isCancelled() && event.getDamage() > 0) {
            ItemUseAnimation anim = CraftItemStack.unwrap(e.getActiveItem()).getUseAnimation();
            if (anim == ItemUseAnimation.EAT || anim == ItemUseAnimation.DRINK) {
                e.clearActiveItem();
            }
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE ||
                event instanceof EntityDamageByEntityEvent || event.getDamageSource().getDamageType() == DamageType.MOB_PROJECTILE ||
                event.getDamageSource().getDamageType() == DamageType.GENERIC_KILL || event.getDamageSource().getDamageType() == DamageType.GENERIC) return;

        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

        double invulDamage = invulnerabilityScheduler.isInvulnerable(livingEntity);

        if (event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL &&
                invulnerabilityScheduler.getLastDamageCause(livingEntity) == EntityDamageEvent.DamageCause.FLY_INTO_WALL &&
                invulDamage > 0) {
            event.setCancelled(true);
            return;
        }

        if (invulDamage >= event.getDamage() && invulDamage != 0) {
            event.setCancelled(true);
            return;
        }

        double originalDamage = event.getDamage();
        event.setDamage(event.getDamage() - invulDamage);

        if (event.getCause() == EntityDamageEvent.DamageCause.MAGIC && event.getDamageSource().getDirectEntity() == null && event.getDamageSource().getCausingEntity() == null) {
            event.setDamage(event.getDamage() * .125);
        }

        // If the damage is not from an entity, set the entity to be invulnerable for 10 ticks
        if (!(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK))
            invulnerabilityScheduler.setInvulnerable(livingEntity, 10, originalDamage, event.getCause());

        if (event.isCancelled()) return;

        if (event.getEntity() instanceof LivingEntity e && computeAmountFromEntityDamageEvent(event) > 0) {
            net.minecraft.world.entity.LivingEntity nms = ((CraftLivingEntity) e).getHandle();
            DamageSource damageSource = ((CraftDamageSource) event.getDamageSource()).getHandle();
            if (!damageSource.is(DamageTypeTags.NO_IMPACT)) nms.hurtMarked = true;
            if (!damageSource.is(DamageTypeTags.NO_KNOCKBACK)) {
                double d = 0.0F;
                double d1 = 0.0F;
                Entity attacker = damageSource.getDirectEntity();
                if (attacker instanceof Projectile projectile) {
                    DoubleDoubleImmutablePair doubleDoubleImmutablePair = projectile.calculateHorizontalHurtKnockbackDirection(nms, damageSource);
                    d = -doubleDoubleImmutablePair.leftDouble();
                    d1 = -doubleDoubleImmutablePair.rightDouble();
                } else if (damageSource.getSourcePosition() != null) {
                    d = damageSource.getSourcePosition().x() - nms.getX();
                    d1 = damageSource.getSourcePosition().z() - nms.getZ();
                }

                if (Math.abs(d) > (double) 200.0F) {
                    d = Math.random() - Math.random();
                }

                if (Math.abs(d1) > (double) 200.0F) {
                    d1 = Math.random() - Math.random();
                }
                nms.indicateDamage(d, d1);
            }
        }
    }
    private float computeAmountFromEntityDamageEvent(EntityDamageEvent event) {
        float amount = 0.0F;
        if (event.getDamageSource().getDirectEntity() instanceof org.bukkit.entity.Projectile && event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) != 0.0) return 0.0F;
        amount += (float)event.getDamage(EntityDamageEvent.DamageModifier.BASE);
        amount += (float)event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING);
        amount += (float)event.getDamage(EntityDamageEvent.DamageModifier.FREEZING);
        amount += (float)event.getDamage(EntityDamageEvent.DamageModifier.HARD_HAT);
        return amount;
    }
}
