package com.dudeturtles.combatium.listeners.util;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.logging.Logger;

public class EntityMovementOptimization implements Listener {
    private final HashSet<Integer> entityIds = new HashSet<>();
    private final MinecraftServer server;
    private final Logger logger;

    public EntityMovementOptimization(MinecraftServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKnockback(EntityKnockbackEvent event) {
        Entity e = ((CraftEntity) event.getEntity()).getHandle();
        entityIds.add(e.getId());
    }

    @EventHandler
    public void onTickEnd(ServerTickEndEvent event) {
        for (Level level : server.getAllLevels()) {
            newTrackerTick(level);
        }
    }

    private void newTrackerTick(Level level) {
        final ca.spottedleaf.moonrise.patches.chunk_system.level.entity.server.ServerEntityLookup entityLookup = (ca.spottedleaf.moonrise.patches.chunk_system.level.entity.server.ServerEntityLookup)((ca.spottedleaf.moonrise.patches.chunk_system.level.ChunkSystemServerLevel)level).moonrise$getEntityLookup();

        final ca.spottedleaf.moonrise.common.list.ReferenceList<net.minecraft.world.entity.Entity> trackerEntities = entityLookup.trackerEntities;
        final Entity[] trackerEntitiesRaw = trackerEntities.getRawDataUnchecked();
        for (int i = 0, len = trackerEntities.size(); i < len; ++i) {
            final Entity entity = trackerEntitiesRaw[i];
            if (entityIds.contains(entity.getId())) {
                entity.needsSync = true;
            }

            final ChunkMap.TrackedEntity tracker = ((ca.spottedleaf.moonrise.patches.entity_tracker.EntityTrackerEntity)entity).moonrise$getTrackedEntity();

            tracker.moonrise$tick(((ca.spottedleaf.moonrise.patches.chunk_system.entity.ChunkSystemEntity)entity).moonrise$getChunkData().nearbyPlayers);
            if (tracker.moonrise$hasPlayers()
                    || ((ca.spottedleaf.moonrise.patches.chunk_system.entity.ChunkSystemEntity)entity).moonrise$getChunkStatus().isOrAfter(FullChunkStatus.ENTITY_TICKING)) {
                try {
                    Field tickCount = ServerEntity.class.getDeclaredField("tickCount");
                    Field teleportDelay = ServerEntity.class.getDeclaredField("teleportDelay");
                    tickCount.setAccessible(true);
                    teleportDelay.setAccessible(true);
                    tickCount.set(tracker.serverEntity, ((int) tickCount.get(tracker.serverEntity)) - 1);
                    teleportDelay.set(tracker.serverEntity, ((int) teleportDelay.get(tracker.serverEntity)) - 1);
                } catch (Exception e) {
                    logger.severe("Failed to fetch Tick Count.");
                    logger.severe("That's not good 0.o");
                }
                tracker.serverEntity.sendChanges();
            }
        }

        entityIds.clear();
    }
}
