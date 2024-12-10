package com.combatium.listeners.damage;

import com.combatium.CombatiumLite;
import com.combatium.util.active.KnockbackUtil;
import com.combatium.util.calculations.PlayerHitUtil;
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftWindCharge;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.Objects;

import static com.combatium.util.calculations.FixItem.isSword;

public class KnockbackListener implements Listener {
    @EventHandler
    public void onKnockback(EntityKnockbackByEntityEvent event) {
        if (event.getHitBy() instanceof Player) return;
        if (event.getHitBy() instanceof CraftWindCharge && !CombatiumLite.wackyWindCharges) return;

        double strength = (event.getKnockbackStrength() / (1 - Objects.requireNonNull(event.getEntity().getAttribute(Attribute.KNOCKBACK_RESISTANCE)).getValue())) / 0.6;
        Vector v = event.getEntity().getVelocity();
        Vector k = event.getKnockback().add(v);
        double x = ((v.getX() / 2d) - k.getX()) / event.getKnockbackStrength();
        double z = ((v.getZ() / 2d) - k.getZ()) / event.getKnockbackStrength();

        Vector entityDirection = event.getEntity().getLocation().getDirection();
        double angle = PlayerHitUtil.calculateAngle(new double[]{entityDirection.getX(), 0, entityDirection.getZ()}, new double[]{x, 0, z});

        boolean isBlocking = false;
        boolean isSwordBlocking = false;
        if (event.getEntity() instanceof Player player) {
            if (CombatiumLite.swordBlocking) isSwordBlocking = isSword(player.getActiveItem().getType());
            isBlocking = player.isBlocking();
        }

        boolean isBlocked = isBlocking && angle <= 50;
        boolean isSwordBlocked = isSwordBlocking && angle <= 50;
        if (isSwordBlocked) isBlocked = false;

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

        if ((isBlocked && event.getCause() == EntityKnockbackByEntityEvent.Cause.ENTITY_ATTACK) || event.getCause() == EntityKnockbackEvent.Cause.SHIELD_BLOCK) return;

        KnockbackUtil.applyKnockback(event.getEntity(), strength, x, z, isBlocked, isSwordBlocked);
        if (!event.getEntity().isOnGround()) event.getEntity().setVelocity(event.getEntity().getVelocity().setY((event.getEntity().getVelocity().getY() - 0.08)));
        event.setCancelled(true);
    }
}
