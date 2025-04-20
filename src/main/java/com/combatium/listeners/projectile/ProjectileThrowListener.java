package com.combatium.listeners.projectile;

import com.combatium.util.active.TridentTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftAbstractArrow;
import org.bukkit.craftbukkit.entity.CraftProjectile;
import org.bukkit.craftbukkit.entity.CraftWindCharge;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileThrowListener implements Listener {
    @EventHandler
    public void onProjectileThrow(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() != null) {
            if (event.getEntity().getShooter() instanceof org.bukkit.entity.Player player) {
                if (event.getEntity() instanceof CraftWindCharge) return;
                Player p = ((org.bukkit.craftbukkit.entity.CraftPlayer) player).getHandle();
                Projectile proj;
                if (event.getEntity() instanceof CraftAbstractArrow) {
                    proj = ((CraftAbstractArrow) event.getEntity()).getHandle();
                } else {
                    proj = ((CraftProjectile) event.getEntity()).getHandle();
                }

                Vec3 knownMovement = p.getKnownMovement();
                Vec3 currentMomentum = proj.getDeltaMovement().subtract(knownMovement.x, p.onGround() ? 0.0 : knownMovement.y, knownMovement.z);

                Vec3 entityMomentum = p.onGround ? p.getDeltaMovement() : p.getDeltaMovement().multiply(1.0, 0.0, 1.0);
                Vec3 projected = project(entityMomentum, currentMomentum);
                proj.setDeltaMovement(currentMomentum.add(projected));

                if (event.getEntity() instanceof Trident trident && trident.getItemStack().containsEnchantment(org.bukkit.enchantments.Enchantment.LOYALTY)) {
                    TridentTracker.addTrident(trident);
                }
            }
        }
    }

    public static Vec3 project(Vec3 originalVec, Vec3 newVec) {
        Vec3 normalized = newVec.normalize();
        double d = originalVec.dot(newVec);
        return normalized.multiply(d, d, d);
    }
}
