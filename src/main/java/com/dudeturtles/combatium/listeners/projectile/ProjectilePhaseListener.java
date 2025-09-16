package com.dudeturtles.combatium.listeners.projectile;

import com.dudeturtles.combatium.CombatiumLite;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

/*
 * Credit to CatPVP for this code.
 */

public class ProjectilePhaseListener implements Listener {
    private static final Set<Projectile> projectiles = new HashSet<>();
    public static void tick() {
        //if(true) return;
        for (Iterator<Projectile> iterator = projectiles.iterator(); iterator.hasNext(); ) {
            Projectile proj = iterator.next();
            World world = proj.getWorld();

            Vector arrowVelocity = proj.getVelocity().clone();
            arrowVelocity.setY(arrowVelocity.getY() - 0.08);
            BoundingBox arrowBox = proj.getBoundingBox().clone()
                    .union(proj.getBoundingBox().clone().shift(arrowVelocity));

            for (Entity entity : world.getNearbyEntities(arrowBox.clone().expand(5, 5, 5))) {
                if (entity instanceof Player p
                        && !p.getGameMode().equals(GameMode.SPECTATOR)
                        && p != proj.getShooter()
                        && proj.getVelocity().getX() != 0
                        && proj.getVelocity().getZ() != 0
                ) {
                    Vector pVelocity = p.getVelocity().clone();
                    pVelocity.setY(pVelocity.getY() - 0.08);

                    BoundingBox playerBox = p.getBoundingBox().clone()
                            .union(p.getBoundingBox().clone().shift(p.getVelocity()));

                    if (arrowBox.overlaps(playerBox)) {
                        iterator.remove();
                        proj.hitEntity(entity, proj.getVelocity());
                        //Bukkit.broadcastMessage("hitting: " + entity.getName());
                    }
                }
            }
        }
    }

    public ProjectilePhaseListener() {
        CombatiumLite.getScheduler().registerProjectilePhaseListener(this);
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Projectile proj ) {
            projectiles.add(proj);
            Bukkit.getScheduler().runTaskLater(CombatiumLite.getInstance(), () -> projectiles.remove(proj), 20L * 30L);
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        //event.setCancelled(true);
        projectiles.remove(event.getEntity());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(true) return;
        Player p = event.getPlayer();
        for (Entity entity : p.getNearbyEntities(5,5,5)) {
            if(!(entity instanceof Projectile proj)) continue;
            if (!projectiles.contains(entity)) continue;
            if (p == proj.getShooter()) continue;
            if (proj.getVelocity().getX() == 0 && proj.getVelocity().getZ() == 0) continue;

            Vector arrowVelocity = proj.getVelocity().clone();
            arrowVelocity.setY(arrowVelocity.getY()-0.08);

            Vector pVelocity = event.getTo().toVector().subtract(event.getFrom().toVector());
            //Vector pVelocity = p.getVelocity().clone();
            //pVelocity.setY(pVelocity.getY()-0.08);

            BoundingBox arrowBox = proj.getBoundingBox().clone()
                    .union(proj.getBoundingBox().clone().shift(arrowVelocity));

            BoundingBox playerBox = p.getBoundingBox().clone()
                    .union(p.getBoundingBox().clone().shift(pVelocity));
            if(playerBox.overlaps(arrowBox)) {
                //Bukkit.broadcastMessage("move hitting: " + entity.getName());
                proj.hitEntity(p, proj.getVelocity());
                projectiles.remove(proj);
            }
            //event.setCancelled(true);
        }
    }


    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Arrow proj) projectiles.remove(proj);
    }
}

