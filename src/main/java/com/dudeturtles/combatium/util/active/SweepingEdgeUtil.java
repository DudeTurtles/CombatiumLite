package com.dudeturtles.combatium.util.active;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.BoundingBox;
import javax.annotation.Nullable;

public class SweepingEdgeUtil {
    public static void sweepAttack(Player player, @Nullable LivingEntity hit) {
        if (player == null) return;
        if (hit != null && player.isSprinting() && !player.isInsideVehicle() && !player.isUnderWater()) return;
        if (player.getFallDistance() > 0 && !player.isInsideVehicle() && !player.isOnGround() && !player.isFlying()) return;

        addParticle(player);

        if (hit != null) {
            hit.getLocation().getNearbyLivingEntities(1, 0.25, 1).stream()
                    .filter(entity -> entity != player && entity != hit)
                    .forEach(entity -> PlayerDamageEntity.playerDamageEntityEvent(player, entity, player.getInventory().getItemInMainHand(), EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, true));
        } else {
            double dirX = (-Mth.sin(player.getYaw() * 0.017453292F)) * 2.0;
            double dirZ = Mth.cos(player.getYaw() * 0.017453292F) * 2.0;
            BoundingBox hitbox = player.getBoundingBox().expand(1.0, 0.25, 1.0).shift(dirX, 0.0d, dirZ);

            player.getWorld().getNearbyEntities(hitbox).stream()
                    .filter(e -> e instanceof LivingEntity && e != player)
                    .forEach(entity -> PlayerDamageEntity.playerDamageEntityEvent(player, (LivingEntity) entity, player.getInventory().getItemInMainHand(), EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, true));
        }
    }

    public static boolean hasSweeping(Player player) {
        return ((CraftPlayer)player).getHandle().getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) > 0;
    }

    public static void addParticle(Player player) {
        double dirX = -Mth.sin(player.getYaw() * 0.017453292F);
        double dirZ = Mth.cos(player.getYaw() * 0.017453292F);
        player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, player.getX() + dirX, player.getY() + (player.getHeight() * 0.5), player.getZ() + dirZ, 1);
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1);
    }
}
