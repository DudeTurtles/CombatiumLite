package com.combatium.listeners.projectile;

import com.combatium.CombatiumLite;
import com.combatium.util.active.KnockbackUtil;
import com.combatium.util.calculations.PlayerHitUtil;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import static com.combatium.util.calculations.FixItem.isSword;

public class ProjectileHitListener implements Listener {
    @EventHandler
    public void onSnowball(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof LivingEntity entity) {
            if (event.getEntity() instanceof Egg || event.getEntity() instanceof Snowball || event.getEntity() instanceof EnderPearl) {
                net.minecraft.world.entity.LivingEntity e = ((CraftLivingEntity) entity).getHandle();
                net.minecraft.world.entity.LivingEntity attacker = null;
                if (event.getEntity().getShooter() instanceof LivingEntity livingEntity) attacker = ((CraftLivingEntity) livingEntity).getHandle();

                Vector v = event.getEntity().getVelocity().multiply(-1);
                double angle = PlayerHitUtil.calculateAngle(new double[]{entity.getLocation().getDirection().getX(), 0, entity.getLocation().getDirection().getZ()},
                        new double[]{v.getX(), 0, v.getZ()});

                boolean isSwordBlocking = false;
                if (CombatiumLite.swordBlocking) isSwordBlocking = isSword(entity.getActiveItem().getType());
                boolean isBlocking = false;
                if (entity instanceof Player player) isBlocking = player.isBlocking();

                boolean isBlocked = isBlocking && angle <= 50;
                boolean isSwordBlocked = isSwordBlocking && angle <= 50;
                if (isSwordBlocked) isBlocked = false;

                if (isBlocked) {
                    event.getEntity().getWorld().playSound(event.getEntity().getLocation(), org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1, 1);
                    event.setCancelled(true);
                    return;
                }

                e.hurt((attacker == null) ? e.damageSources().genericKill() : e.damageSources().genericKill().customEventDamager(attacker), 0.000000001f);
                KnockbackUtil.applyKnockback(entity, 1f/2f, v.getX(), v.getZ(), isBlocked, isSwordBlocked, true);
                if (event.getEntity().getShooter() instanceof LivingEntity attacker1) PlayerHitUtil.thornsCheck(entity, attacker1);
            }
        }
    }
}
