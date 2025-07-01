package com.combatium.util.active;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import java.util.Objects;
import static org.bukkit.Sound.ITEM_MACE_SMASH_GROUND;
import static org.bukkit.Sound.ITEM_MACE_SMASH_GROUND_HEAVY;

public class MaceUtil {
    public static boolean macePostHit(ItemStack mace, float fallHeight, Player attacker, Entity target) {
        if (mace.getType() != Material.MACE || fallHeight < 1.5 || attacker.isGliding()) return false;

        if (target.isOnGround()) {
            Sound event = fallHeight > 5.0f ? ITEM_MACE_SMASH_GROUND_HEAVY : ITEM_MACE_SMASH_GROUND;
            attacker.getWorld().playSound(attacker, event, SoundCategory.PLAYERS, 1.0f, 1.0f);
        } else {
            attacker.getWorld().playSound(attacker, Sound.ITEM_MACE_SMASH_AIR, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }

        target.getLocation().getNearbyLivingEntities(3.5, 3.5, 3.5).forEach(entity -> {
            try {
                Vector direction = entity.getLocation().subtract(target.getLocation()).toVector();
                double d = getKnockback(attacker, entity, direction);
                Vector vector = direction.normalize().multiply(d);
                if (d > 0) {
                    vector.setY(0.7f);
                    entity.setVelocity(entity.getVelocity().add(vector));
                }
            } catch (Exception ignored) {}
        });

        spawnBlockParticleCircle(target.getLocation(), target.getLocation().subtract(0, 0.1, 0).getBlock().getType(), 1, 10, 0.5);

        spawnBlockParticleCircle(target.getLocation(), target.getLocation().subtract(0, 0.1, 0).getBlock().getType(), 3, 25, 0.6);

        attacker.setVelocity(new Vector(0, 0, 0));
        attacker.setFallDistance(0);

        windburstPlayer(attacker, mace.getEnchantmentLevel(Enchantment.WIND_BURST));
        return true;
    }

    private static void windburstPlayer(Player player, int level) {
        if (level <= 0) return;

        player.getLocation().getWorld().spawnParticle(Particle.GUST_EMITTER_LARGE, player.getLocation(), 1, 1.0, 1.0, 1.0, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, SoundCategory.PLAYERS, 1.0f, 1.0f);
        player.setVelocity(player.getVelocity().setY(1.15 + 0.35 * level));
    }

    private static double getKnockback(Player attacker, LivingEntity attacked, Vector distance) {
        return (3.5 - distance.length()) * 0.699999988079071 * (attacker.getFallDistance() > 5.0F ? 2 : 1) * (1.0 - Objects.requireNonNull(attacked.getAttribute(Attribute.KNOCKBACK_RESISTANCE)).getValue());
    }

    // Used for spawning a circle of block particles around a location
    private static void spawnBlockParticleCircle(Location center, Material blockType, double radius, int count, double offset) {
        World world = center.getWorld();
        if (world == null) return;

        // Generate particles around a circle by varying the angle
        for (double angle = 0; angle < 360; angle += 10) { // Adjust step angle for particle density
            // Calculate x and z offsets based on the angle and radius
            double xOffset = radius * Math.cos(Math.toRadians(angle));
            double zOffset = radius * Math.sin(Math.toRadians(angle));

            // Create the particle location
            Location particleLocation = center.clone().add(xOffset, 0, zOffset);

            // Spawn block particle at calculated location
            for (int i = 0; i < count; i++) {
                Location pLoc = particleLocation.clone().add((Math.random() - 0.5) * 2 * offset, (Math.random() - 0.5) * 2 * offset, (Math.random() - 0.5) * 2 * offset);
                world.spawnParticle(Particle.BLOCK, pLoc, 0, 0.1, 2.5, 0.1, 1, blockType.createBlockData());
            }
        }
    }
}
