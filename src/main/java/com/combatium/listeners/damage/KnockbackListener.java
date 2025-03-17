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
import org.bukkit.util.Vector;
import java.util.Objects;

import static com.combatium.util.calculations.FixItem.isSword;
import static io.papermc.paper.event.entity.EntityKnockbackEvent.Cause.*;

public class KnockbackListener implements Listener {
    @EventHandler
    public void onKnockback(EntityKnockbackByEntityEvent event) {
        EntityKnockbackEvent.Cause cause = event.getCause();
        if (!(cause == ENTITY_ATTACK || cause == SWEEP_ATTACK)) return;

        boolean isBlocked = false;
        if (event.getEntity().getLastDamageCause() != null) {
            Vec3 vector = ((CraftDamageSource) event.getEntity().getLastDamageCause().getDamageSource()).getHandle().getSourcePosition();
            if (vector != null) {
                net.minecraft.world.entity.Entity nmsEntity = ((CraftLivingEntity) event.getEntity()).getHandle();
                Vec3 balls = nmsEntity.calculateViewVector(0.0f, nmsEntity.getYHeadRot());
                Vec3 directionVector = vector.vectorTo(nmsEntity.position());
                directionVector = directionVector.subtract(0, directionVector.y(), 0).normalize();
                double angle = directionVector.dot(balls) * Math.PI;
                isBlocked = (angle < Math.toRadians(CombatiumLite.shieldProtectionArc - 180)) || CombatiumLite.shieldProtectionArc == 360;
            }
        }

        boolean isBlocking = false;
        boolean isSwordBlocking = false;
        if (event.getEntity() instanceof Player player) {
            if (CombatiumLite.swordBlocking) isSwordBlocking = isSword(player.getActiveItem().getType());
            isBlocking = player.isBlocking();
        }

        isBlocked &= isBlocking;
        boolean isSwordBlocked = isSwordBlocking && isBlocked;
        if (isSwordBlocked) isBlocked = false;

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

        KnockbackUtil.applyKnockback(event.getEntity(), strength, x, z, isBlocked, isSwordBlocked, true);
        event.setCancelled(true);
    }
}
