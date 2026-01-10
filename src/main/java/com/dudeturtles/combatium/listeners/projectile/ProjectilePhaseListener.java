package com.dudeturtles.combatium.listeners.projectile;

import com.dudeturtles.combatium.CombatiumLite;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftProjectile;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
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
        for (Iterator<Projectile> iterator = projectiles.iterator(); iterator.hasNext(); ) {
            Projectile proj = iterator.next();
            World world = proj.getWorld();

            net.minecraft.world.entity.projectile.Projectile nmsProj = ((CraftProjectile) proj).getHandle();
            Vec3 aVelocity = nmsProj.getDeltaMovement().subtract(0, nmsProj.getGravity(), 0);
            if (nmsProj.onGround() ? nmsProj.getDeltaMovement().horizontalDistance() <= 0 : nmsProj.getDeltaMovement().length() <= 0) continue;

            AABB projCurrentBox = nmsProj.getBoundingBox();
            AABB projNextBox = nmsProj.getBoundingBox().move(aVelocity);
            CraftPlayer playerShooter = proj.getShooter() instanceof CraftPlayer shooter ? shooter : null;

            for (int cut = 1; cut <= cuts; cut++) {
                float progress = (float) cut / cuts;
                BoundingBox arrowBox = lerpBoxes(progress, projCurrentBox, projNextBox);
                for (Player p : ((CraftWorld) world).getHandle().getEntitiesOfClass(Player.class,
                        toNMS(arrowBox.clone().expand(5)),
                        target -> !target.isSpectator()
                                && (playerShooter == null || target != playerShooter.getHandle()))) {
                    Vec3 pVelocity = p.getDeltaMovement().subtract(0, p.getGravity(), 0);

                    AABB currentBox = p.getBoundingBox();
                    AABB nextBox = p.getBoundingBox().move(pVelocity);

                    BoundingBox playerBox = lerpBoxes(progress, currentBox, nextBox);

                    if (arrowBox.overlaps(playerBox)) {
                        iterator.remove();
                        proj.hitEntity(p.getBukkitEntity(), proj.getVelocity());
                        //Bukkit.broadcastMessage("hitting: " + entity.getName());
                    }
                }
            }
        }
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

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Vector pVelocity = event.getTo().toVector().subtract(event.getFrom().toVector());
        org.bukkit.entity.Player p = event.getPlayer();
        Player nmsPlayer = ((CraftPlayer) p).getHandle();
        AABB pCurrentBox = nmsPlayer.getBoundingBox();
        AABB pNextBox = nmsPlayer.getBoundingBox().move(new Vec3(pVelocity.getX(), pVelocity.getY(), pVelocity.getZ()));
        for (int cut = 1; cut <= cuts; cut++) {
            float progress = (float) cut / cuts;
            BoundingBox playerBox = lerpBoxes(progress, pCurrentBox, pNextBox);
            for (net.minecraft.world.entity.projectile.Projectile nmsProj : nmsPlayer.level().getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,
                    toNMS(playerBox.clone().expand(5)),
                    proj -> nmsPlayer != proj.getOwner()
                            && (proj.onGround() ? proj.getDeltaMovement().horizontalDistance() <= 0 : proj.getDeltaMovement().length() <= 0)
                            && proj.getBukkitEntity() instanceof Projectile bukkit
                            && projectiles.contains(bukkit))) {
                Vec3 aVelocity = nmsProj.getDeltaMovement().subtract(0, nmsProj.getGravity(), 0);

                AABB projCurrentBox = nmsProj.getBoundingBox();
                AABB projNextBox = nmsProj.getBoundingBox().move(aVelocity);

                BoundingBox arrowBox = lerpBoxes(progress, projCurrentBox, projNextBox);

                if (arrowBox.overlaps(playerBox)) {
                    ((Projectile) nmsProj.getBukkitEntity()).hitEntity(p, nmsProj.getBukkitEntity().getVelocity());
                    //Bukkit.broadcastMessage("hitting: " + entity.getName());
                }
            }
        }
    }

    public static AABB toNMS(BoundingBox bb) {
        return new AABB(bb.getMinX(), bb.getMinY(), bb.getMinZ(), bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());
    }

    public static BoundingBox lerpBoxes(float delta, AABB oldBox, AABB newBox) {
        return new BoundingBox(Mth.lerp(delta, oldBox.minX, newBox.minX),
                Mth.lerp(delta, oldBox.minY, newBox.minY),
                Mth.lerp(delta, oldBox.minZ, newBox.minZ),
                Mth.lerp(delta, oldBox.maxX, newBox.maxX),
                Mth.lerp(delta, oldBox.maxY, newBox.maxY),
                Mth.lerp(delta, oldBox.maxZ, newBox.maxZ));
    }
}

