package com.dudeturtles.combatium.listeners.projectile;

import com.dudeturtles.combatium.CombatiumLite;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.AbstractProjectile;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
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
    private static final Set<Projectile> projectiles = Collections.newSetFromMap(new WeakHashMap<>());
    private static final int cuts = 5;
    public static void tick() {
        List<Projectile> toRemove = new ArrayList<>();
        for (Projectile proj : projectiles) {
            World world = proj.getWorld();

            net.minecraft.world.entity.projectile.Projectile nmsProj = ((AbstractProjectile) proj).getHandle();
            Vec3 aVelocity = nmsProj.getDeltaMovement().subtract(0, nmsProj.getGravity(), 0);
            if (nmsProj.onGround() ? nmsProj.getDeltaMovement().horizontalDistance() <= 0 : nmsProj.getDeltaMovement().length() <= 0)
                continue;

            AABB projCurrentBox = nmsProj.getBoundingBox();
            AABB projNextBox = nmsProj.getBoundingBox().move(aVelocity);
            CraftPlayer playerShooter = proj.getShooter() instanceof CraftPlayer shooter ? shooter : null;

            for (Player p : ((CraftWorld) world).getHandle().getEntitiesOfClass(Player.class,
                    projCurrentBox.expandTowards(aVelocity).inflate(1.5),
                    target -> !target.isSpectator()
                            && (playerShooter == null || target != playerShooter.getHandle()))) {
                for (int cut = 1; cut <= cuts; cut++) {
                    float progress = (float) cut / cuts;
                    BoundingBox arrowBox = lerpBoxes(progress, projCurrentBox, projNextBox);
                    Vec3 pVelocity = p.getDeltaMovement().subtract(0, p.getGravity(), 0);

                    AABB currentBox = p.getBoundingBox();
                    AABB nextBox = p.getBoundingBox().move(pVelocity);

                    BoundingBox playerBox = lerpBoxes(progress, currentBox, nextBox);

                    if (boxesOverlap(arrowBox, playerBox)) {
                        toRemove.add(proj);
                        proj.hitEntity(p.getBukkitEntity(), proj.getVelocity());
                        //Bukkit.broadcastMessage("hitting: " + entity.getName());
                        break;
                    }
                }
            }
        }
        toRemove.forEach(projectiles::remove);
    }

    public ProjectilePhaseListener() {
        CombatiumLite.getScheduler().registerProjectilePhaseListener(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Projectile proj) {
            projectiles.add(proj);
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        projectiles.remove(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Vector pVelocity = event.getTo().toVector().subtract(event.getFrom().toVector());
        org.bukkit.entity.Player p = event.getPlayer();
        Player nmsPlayer = ((CraftPlayer) p).getHandle();
        AABB pCurrentBox = nmsPlayer.getBoundingBox();
        AABB pNextBox = nmsPlayer.getBoundingBox().move(new Vec3(pVelocity.getX(), pVelocity.getY(), pVelocity.getZ()));
        for (net.minecraft.world.entity.projectile.Projectile nmsProj : nmsPlayer.level().getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,
                pCurrentBox.expandTowards(pVelocity.getX(), pVelocity.getY(), pVelocity.getZ()).inflate(1.5),
                proj -> nmsPlayer != proj.getOwner()
                        && (proj.onGround() ? proj.getDeltaMovement().horizontalDistance() <= 0 : proj.getDeltaMovement().length() <= 0)
                        && proj.getBukkitEntity() instanceof Projectile bukkit
                        && projectiles.contains(bukkit))) {
            for (int cut = 1; cut <= cuts; cut++) {
                float progress = (float) cut / cuts;
                BoundingBox playerBox = lerpBoxes(progress, pCurrentBox, pNextBox);
                Vec3 aVelocity = nmsProj.getDeltaMovement().subtract(0, nmsProj.getGravity(), 0);

                AABB projCurrentBox = nmsProj.getBoundingBox();
                AABB projNextBox = nmsProj.getBoundingBox().move(aVelocity);

                BoundingBox arrowBox = lerpBoxes(progress, projCurrentBox, projNextBox);

                if (boxesOverlap(arrowBox, playerBox)) {
                    ((Projectile) nmsProj.getBukkitEntity()).hitEntity(p, nmsProj.getBukkitEntity().getVelocity());
                    //Bukkit.broadcastMessage("hitting: " + entity.getName());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onRemoveEntity(EntityRemoveEvent event) {
        if (event.getEntity() instanceof Projectile proj) projectiles.remove(proj);
    }

    public static BoundingBox lerpBoxes(float delta, AABB oldBox, AABB newBox) {
        return new BoundingBox(Mth.lerp(delta, oldBox.minX, newBox.minX),
                Mth.lerp(delta, oldBox.minY, newBox.minY),
                Mth.lerp(delta, oldBox.minZ, newBox.minZ),
                Mth.lerp(delta, oldBox.maxX, newBox.maxX),
                Mth.lerp(delta, oldBox.maxY, newBox.maxY),
                Mth.lerp(delta, oldBox.maxZ, newBox.maxZ));
    }
    public static boolean boxesOverlap(BoundingBox a, BoundingBox b) {
        return (a.getMinX() <= b.getMaxX() && a.getMaxX() >= b.getMinX())
        && (a.getMinY() <= b.getMaxY() && a.getMaxY() >= b.getMinY())
        && (a.getMinZ() <= b.getMaxZ() && a.getMaxZ() >= b.getMinZ());
    }
}

