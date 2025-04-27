package com.combatium.listeners.projectile;

import com.combatium.CombatiumLite;
import com.combatium.util.active.KnockbackUtil;
import com.combatium.util.calculations.FixItem;
import com.combatium.util.calculations.PlayerHitUtil;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import net.minecraft.world.phys.Vec3;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftProjectile;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import java.util.logging.Logger;

public class ProjectileHitListener implements Listener {
    @EventHandler
    public void onSnowball(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof LivingEntity entity) {
            if (!(event.getHitEntity() instanceof Player)) entity.setMaximumNoDamageTicks(0);
            if (event.getEntity() instanceof Egg || event.getEntity() instanceof Snowball || event.getEntity() instanceof EnderPearl) {
                net.minecraft.world.entity.LivingEntity nmsEntity = ((CraftLivingEntity) entity).getHandle();
                net.minecraft.world.entity.projectile.Projectile nmsProjectile = ((CraftProjectile) event.getEntity()).getHandle();
                net.minecraft.world.entity.LivingEntity attacker = null;
                if (event.getEntity().getShooter() instanceof LivingEntity livingEntity) attacker = ((CraftLivingEntity) livingEntity).getHandle();

                Vec3 vector = nmsProjectile.position();
                Vec3 balls = nmsEntity.calculateViewVector(0.0f, nmsEntity.getYHeadRot());
                Vec3 directionVector = vector.vectorTo(nmsEntity.position());
                directionVector = directionVector.subtract(0, directionVector.y(), 0).normalize();
                double angle = directionVector.dot(balls) * Math.PI;
                boolean isBlocked = (angle < Math.toRadians(CombatiumLite.shieldProtectionArc - 180)) || CombatiumLite.shieldProtectionArc == 360;

                boolean isBlocking = false;
                boolean isSwordBlocking = false;
                if (event.getHitEntity() instanceof Player player) {
                    if (CombatiumLite.swordBlocking) isSwordBlocking = FixItem.isSword(player.getActiveItem().getType());
                    isBlocking = player.isBlocking();
                }

                isBlocked &= isBlocking;
                boolean isSwordBlocked = isSwordBlocking && isBlocked;
                if (isSwordBlocked) isBlocked = false;

                if (isBlocked) {
                    event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
                }

                DoubleDoubleImmutablePair doubleDoubleImmutablePair = nmsProjectile.calculateHorizontalHurtKnockbackDirection(nmsEntity, nmsProjectile.damageSources().generic());
                double d = -doubleDoubleImmutablePair.leftDouble();
                double d1 = -doubleDoubleImmutablePair.rightDouble();

                try {
                    if (entity instanceof Player p) {
                        if (p.getGameMode() != GameMode.SPECTATOR && p.getGameMode() != GameMode.CREATIVE) nmsEntity.hurt((attacker == null) ? nmsEntity.damageSources().genericKill() : nmsEntity.damageSources().genericKill().eventEntityDamager(attacker), 0.000000001f);
                    } else nmsEntity.hurt((attacker == null) ? nmsEntity.damageSources().genericKill() : nmsEntity.damageSources().genericKill().eventEntityDamager(attacker), 0.000000001f);
                } catch (NoSuchMethodError e) {
                    nmsEntity.hurt(nmsEntity.damageSources().genericKill(), 0.000000001f);
                    Logger logger = CombatiumLite.getPluginLogger();
                    logger.severe("Failed to apply proper damage. Please report this to the CombatiumLite developers.");
                    logger.severe("This is not a critical error, but it may affect gameplay.");
                    logger.severe("Error: " + e.getMessage());
                }
                KnockbackUtil.applyKnockback(entity, 0.4f, d, d1, isBlocked, 0, true);
                if (event.getEntity().getShooter() instanceof LivingEntity attacker1) PlayerHitUtil.thornsCheck(entity, attacker1);
            }
        }
    }
}
