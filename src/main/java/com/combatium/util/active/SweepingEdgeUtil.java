package com.combatium.util.active;

import net.minecraft.util.Mth;
import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.BoundingBox;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class SweepingEdgeUtil {
    public static void Sweep(Player player, @Nullable LivingEntity hit) {
        if (player == null) return;
        if (hit != null && (!player.isOnGround() || player.isSprinting())) return;

        addParticle(player);

        if (hit != null) {
            //Damage value doesn't matter since it will be recalculated later
            DamageSource damageSource = DamageSource.builder(DamageType.PLAYER_ATTACK).withDamageLocation(player.getLocation()).withDirectEntity(player).build();

            hit.getNearbyEntities(1, 0.25, 1).stream()
                    .filter(entity -> entity instanceof LivingEntity && entity != player)
                    .map(entity -> (LivingEntity) entity)
                    .forEach(entity -> {
                        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, entity, EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, damageSource, 2);
                        Bukkit.getServer().getPluginManager().callEvent(event);
                    });
        } else {
            double dirX = (-Mth.sin(player.getYaw() * 0.017453292F)) * 2.0;
            double dirZ = Mth.cos(player.getYaw() * 0.017453292F) * 2.0;
            BoundingBox hitbox = player.getBoundingBox().expand(1.0, 0.25, 1.0).shift(dirX, 0.0d, dirZ);

            ArrayList<LivingEntity> entities = new ArrayList<>();

            for (Entity entity : player.getWorld().getNearbyEntities(hitbox)) {
                if (!(entity instanceof LivingEntity) || entity == player) continue;
                entities.add((LivingEntity) entity);
            }

            DamageSource damageSource = DamageSource.builder(DamageType.PLAYER_ATTACK).withDamageLocation(player.getLocation()).withDirectEntity(player).build();
            for (LivingEntity entity : entities) {
                EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, entity, EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, damageSource, 2);
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
        }
    }

    public static int getSweepingLevel(Player player) {
        return player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.SWEEPING_EDGE);
    }

    public static void addParticle(Player player) {
        double dirX = -Mth.sin(player.getYaw() * 0.017453292F);
        double dirZ = Mth.cos(player.getYaw() * 0.017453292F);
        player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, player.getX() + dirX, player.getY() + (player.getHeight() * 0.5), player.getZ() + dirZ, 1);
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1);
    }
}
