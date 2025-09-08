package com.dudeturtles.combatium.util.active;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.util.ping.PingManager;
import com.dudeturtles.combatium.util.ping.PlayerPingData;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import java.util.Objects;
import java.util.logging.Logger;

public class KnockbackUtil {
    public static void applyKnockback(LivingEntity entity, double strength, double x, double z, boolean first) {
        if (entity instanceof Player p && (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR))
            return;
        net.minecraft.world.entity.LivingEntity nmsEntity = ((CraftLivingEntity) entity).getHandle();
        double knockbackRes = Objects.requireNonNull(entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE)).getValue();
        knockbackRes = Math.min(1.0, knockbackRes + (nmsEntity.isBlocking() ? getBlockingReduction(entity) : 0.0));

        strength *= 1.0 - knockbackRes;
        if (strength <= 0.0F) return;
        try {
            Vector delta = entity.getVelocity();
            Vector diff = (new Vector(x, 0.0, z)).normalize().multiply(strength);

            double vertical = delta.getY();
            boolean onGround = entity.isOnGround();

            if (entity instanceof Player && CombatiumLite.pingImprovements) {
                PlayerPingData playerPingData = PingManager.getPlayerData(entity.getUniqueId());
                if (playerPingData != null) onGround = playerPingData.isOnGround(entity.getVelocity().getY());

                if (!onGround && playerPingData != null && first) {
                    vertical = playerPingData.estimateVerticalVelocity(vertical);
                }
            }

            Vector velocity = new Vector(delta.getX() / 2.0 - diff.getX(), (onGround ? Math.min(0.4, strength * 0.75) : (Math.min(0.4, vertical + (strength * 0.5)))), delta.getZ() / 2.0 - diff.getZ());
            nmsEntity.hasImpulse = true;
            entity.setVelocity(velocity);
        } catch (Exception e) {
            Logger logger = CombatiumLite.getPluginLogger();
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

    public static double getBlockingReduction(LivingEntity entity) {
        ItemStack item = entity.getActiveItem();
        if (!item.getPersistentDataContainer().has(PlayerDamageEntity.KNOCKBACK_REDUCTION)) return 0.5;
        try {
            return item.getPersistentDataContainer().get(PlayerDamageEntity.KNOCKBACK_REDUCTION, org.bukkit.persistence.PersistentDataType.DOUBLE);
        } catch (Exception e) {
            return 0.5;
        }
    }
}
