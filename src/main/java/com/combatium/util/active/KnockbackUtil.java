package com.combatium.util.active;

import com.combatium.CombatiumLite;
import com.combatium.util.ping.PingManager;
import com.combatium.util.ping.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Objects;

import static com.combatium.CombatiumLite.logger;
import static java.lang.Double.min;

public class KnockbackUtil {
    public static void applyKnockback(LivingEntity entity, double strength, double x, double z, boolean isBlocked, boolean isSwordBlocked) {
        if (isBlocked && !(entity instanceof Player)) return;
        double knockbackRes = Objects.requireNonNull(entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE)).getValue();
        knockbackRes = min(1.0, knockbackRes + (isBlocked ? 0.5 : 0.0) + (isSwordBlocked? 0.3 : 0.0));

        if (Double.isNaN(x)) {
            x = 0.0;
        }
        if (Double.isNaN(z)) {
            z = 0.0;
        }

        strength *= 1.0 - knockbackRes;
        if (strength > 0.0F) {
            try {
                Vector delta = entity.getVelocity();
                Vector diff = (new Vector(x, 0.0, z)).normalize().multiply(strength);

                boolean onGround = entity.isOnGround();
                if (entity instanceof Player && CombatiumLite.pingImprovements) {
                    PlayerData playerData = PingManager.getPlayerData(entity.getUniqueId());
                    if (playerData != null) onGround = playerData.isOnGround(entity.getVelocity().getY());
                }

                if (Double.isNaN(diff.getX())) {
                    diff.setX(0.0);
                }
                if (Double.isNaN(diff.getZ())) {
                    diff.setZ(0.0);
                }

                entity.setVelocity(new Vector(delta.getX() / 2.0 - diff.getX(), onGround ? Math.min(0.4, strength * 0.75) : (Math.min(0.4, delta.getY() + (strength * 0.5))), delta.getZ() / 2.0 - diff.getZ()));
            } catch (Exception e) {
                logger.warning("=== Please Report This to the Developer ===");
                logger.warning("Error applying knockback to entity: " + entity.getName());
                logger.warning("Strength: " + strength);
                logger.warning("X: " + x);
                logger.warning("Z: " + z);
                logger.warning("diff-X: " + (new Vector(x, 0.0, z)).normalize().multiply(strength).getX());
                logger.warning("diff-Z: " + (new Vector(x, 0.0, z)).normalize().multiply(strength).getZ());
                logger.warning("Entity Velocity: " + entity.getVelocity());
                logger.warning("Knockback Resistance: " + knockbackRes);
                logger.warning("===========================================");
            }
        }
    }
}
