package com.combatium.listeners.damage;

import com.combatium.scheduler.InvulnerabilityScheduler;
import net.minecraft.world.item.ItemUseAnimation;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import static com.combatium.scheduler.InvulnerabilityScheduler.getLastDamageCause;

public class EntityDamageListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity e && event instanceof EntityDamageByEntityEvent && !event.isCancelled() && event.getDamage() > 0) {
            ItemUseAnimation anim = CraftItemStack.unwrap(e.getActiveItem()).getUseAnimation();
            if (anim == ItemUseAnimation.EAT || anim == ItemUseAnimation.DRINK) {
                e.clearActiveItem();
            }
        }
        if (!(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE ||
                event instanceof EntityDamageByEntityEvent || event.getDamageSource().getDamageType() == DamageType.MOB_PROJECTILE ||
                event.getDamageSource().getDamageType() == DamageType.GENERIC_KILL || event.getDamageSource().getDamageType() == DamageType.GENERIC)) {
            if (event.getEntity() instanceof LivingEntity livingEntity) {
                double invulDamage = InvulnerabilityScheduler.isInvulnerable(livingEntity);

                if (event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL &&
                        getLastDamageCause(livingEntity) == EntityDamageEvent.DamageCause.FLY_INTO_WALL &&
                        invulDamage > 0) {
                    event.setCancelled(true);
                    return;
                }

                if (invulDamage >= event.getDamage()) {
                    event.setCancelled(true);
                    return;
                }

                double originalDamage = event.getDamage();
                event.setDamage(event.getDamage() - invulDamage);

                if (event.getCause() == EntityDamageEvent.DamageCause.MAGIC && event.getDamageSource().getDirectEntity() == null && event.getDamageSource().getCausingEntity() == null) {
                    event.setDamage(event.getDamage() * .125);
                }

                // If the damage is not from an entity, set the entity to be invulnerable for 10 ticks
                if (!(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) InvulnerabilityScheduler.setInvulnerable(livingEntity, 10, originalDamage, event.getCause());
            }
        }
    }
}
