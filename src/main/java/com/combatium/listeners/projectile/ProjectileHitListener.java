package com.combatium.listeners.projectile;

import com.combatium.CombatiumLite;
import com.combatium.util.calculations.PlayerHitUtil;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.GameMode;
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
                DamageSource potentialSource = nmsProjectile.damageSources().thrown(nmsProjectile, nmsProjectile.getOwner());


                try {
                    if (entity instanceof Player p) {
                        if (p.getGameMode() != GameMode.SPECTATOR && p.getGameMode() != GameMode.CREATIVE) nmsEntity.hurt(potentialSource, 0.000000001f);
                    } else nmsEntity.hurt(potentialSource, 0.000000001f);
                } catch (NoSuchMethodError e) {
                    nmsEntity.hurt(potentialSource, 0.000000001f);
                    Logger logger = CombatiumLite.getPluginLogger();
                    logger.severe("Failed to apply proper damage. Please report this to the CombatiumLite developers.");
                    logger.severe("This is not a critical error, but it may affect gameplay.");
                    logger.severe("Error: " + e.getMessage());
                }

                if (event.getEntity().getShooter() instanceof LivingEntity attacker1) PlayerHitUtil.thornsCheck(entity, attacker1);
            }
        }
    }
}
