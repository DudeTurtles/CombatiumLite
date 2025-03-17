package com.combatium.listeners.damage;

import com.combatium.CombatiumLite;
import com.combatium.util.active.KnockbackUtil;
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.minecraft.world.phys.Vec3;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import java.util.Objects;

import static com.combatium.util.active.PlayerDamageEntity.*;
import static io.papermc.paper.event.entity.EntityKnockbackEvent.Cause.*;

public class KnockbackListener implements Listener {
    @EventHandler
    public void onKnockback(EntityKnockbackByEntityEvent event) {
        EntityKnockbackEvent.Cause cause = event.getCause();
        if (!(cause == ENTITY_ATTACK || cause == SWEEP_ATTACK)) return;

        boolean isBlocked = false;
        double knockbackReduction = 0;
        double blockAngle;

        if (event.getEntity() instanceof Player p && p.isBlocking()) {
            ItemStack shield = p.getActiveItem();

            if (shield.getItemMeta() != null) {
                knockbackReduction = shield.getItemMeta().getPersistentDataContainer().getOrDefault(KNOCKBACK_REDUCTION, PersistentDataType.DOUBLE, 0.5);
                blockAngle = shield.getItemMeta().getPersistentDataContainer().getOrDefault(BLOCK_ANGLE, PersistentDataType.DOUBLE, CombatiumLite.shieldProtectionArc);
            } else {
                knockbackReduction = 0.5;
                blockAngle = CombatiumLite.shieldProtectionArc;
            }

            double angle = 999;

            if (event.getEntity().getLastDamageCause() != null) {
                Vec3 vector = ((CraftDamageSource) event.getEntity().getLastDamageCause().getDamageSource()).getHandle().getSourcePosition();
                if (vector != null) {
                    net.minecraft.world.entity.Entity nmsEntity = ((CraftLivingEntity) event.getEntity()).getHandle();
                    Vec3 balls = nmsEntity.calculateViewVector(0.0f, nmsEntity.getYHeadRot());
                    Vec3 directionVector = vector.vectorTo(nmsEntity.position());
                    directionVector = directionVector.subtract(0, directionVector.y(), 0).normalize();
                    angle = directionVector.dot(balls) * Math.PI;
                }
            }

            if (angle != 999) isBlocked = (angle < blockAngle - 180) || blockAngle >= 360;
        }

        double strength = (event.getKnockbackStrength() / (1 - Objects.requireNonNull(event.getEntity().getAttribute(Attribute.KNOCKBACK_RESISTANCE)).getValue()));
        Vector v = event.getEntity().getVelocity();
        Vector k = event.getKnockback().add(v);
        double x = ((v.getX() / 2d) - k.getX()) / event.getKnockbackStrength();
        double z = ((v.getZ() / 2d) - k.getZ()) / event.getKnockbackStrength();

        if (isBlocked && event.getCause() == EntityKnockbackByEntityEvent.Cause.ENTITY_ATTACK) {
            if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) {
                Entity lastDamager = damageEvent.getDamager();
                if (lastDamager instanceof AbstractArrow arrow) {
                    event.getEntity().getWorld().playSound(event.getEntity().getLocation(), org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1, 1);
                    if (arrow.getPierceLevel() < 1) {
                        event.setCancelled(true);
                        return;
                    }
                } else if (lastDamager instanceof Egg || lastDamager instanceof Snowball) {
                    event.getEntity().getWorld().playSound(event.getEntity().getLocation(), org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1, 1);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        KnockbackUtil.applyKnockback(event.getEntity(), strength, x, z, isBlocked, knockbackReduction, true);
        event.setCancelled(true);
    }
}
