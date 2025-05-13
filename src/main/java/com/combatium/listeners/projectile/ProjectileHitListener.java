package com.combatium.listeners.projectile;

import com.combatium.util.active.KnockbackUtil;
import com.combatium.util.calculations.PlayerHitUtil;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftProjectile;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitListener implements Listener {
    @EventHandler
    public void onSnowball(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof LivingEntity entity) {
            if (!(event.getHitEntity() instanceof Player)) entity.setMaximumNoDamageTicks(0);
            if (event.getEntity() instanceof Egg || event.getEntity() instanceof Snowball || event.getEntity() instanceof EnderPearl) {
                net.minecraft.world.entity.LivingEntity nmsEntity = ((CraftLivingEntity) entity).getHandle();
                net.minecraft.world.entity.projectile.Projectile nmsProjectile = ((CraftProjectile) event.getEntity()).getHandle();
                DamageSource potentialSource = nmsProjectile.damageSources().thrown(nmsProjectile, nmsProjectile.getOwner());

                DoubleDoubleImmutablePair doubleDoubleImmutablePair = nmsProjectile.calculateHorizontalHurtKnockbackDirection(nmsEntity, potentialSource);
                double d = -doubleDoubleImmutablePair.leftDouble();
                double d1 = -doubleDoubleImmutablePair.rightDouble();
                if (!(nmsEntity.getItemBlockingWith() != null && nmsEntity.resolveBlockedDamage(potentialSource, 1) > 0)) nmsEntity.hurtMarked = true;

                KnockbackUtil.applyKnockback(entity, 0.4f, d, d1, true);
                nmsEntity.indicateDamage(d, d1);
                if (event.getEntity().getShooter() instanceof LivingEntity attacker1) PlayerHitUtil.thornsCheck(entity, attacker1);
            }
        }
    }
}
