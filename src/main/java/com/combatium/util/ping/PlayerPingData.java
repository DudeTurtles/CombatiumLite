package com.combatium.util.ping;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PlayerPingData {
    private final Player player;
    public final User user;
    private final Map<Integer, Long> timeline = new HashMap<>();
    private final Random random = new Random();
    private Long lastPing;
    private Long ping;
    private double gravity = 0.08;

    public PlayerPingData(Player player) {
        this.player = player;
        this.user = PacketEvents.getAPI().getPlayerManager().getUser(player);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPing(long ping) {
        this.ping = ping;
    }

    public void setLastPing(long lastPing) {
        this.lastPing = lastPing;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    // Gets the estimated client side vertical velocity of the player based on ping.
    public double estimateVerticalVelocity(double velocity) {
        double ping = (getEstimatedPingNoOffset() / 50d);
        return PingCalculations.estimateVelocity(velocity, ping, gravity);
    }

    public Long getPing() {
        return ping;
    }

    public Map<Integer, Long> getTimeline() {
        return timeline;
    }

    public long getEstimatedPingNoOffset() {
        long current = (ping != null) ? ping : player.getPing();
        long last = (lastPing != null) ? lastPing : player.getPing();
        long ping = (current - last > 20L) ? last : current;
        return (Math.max(1, ping));
    }

    public long getEstimatedPing() {
        long current = (ping != null) ? ping : player.getPing();
        long last = (lastPing != null) ? lastPing : player.getPing();
        long ping = (current - last > 20L) ? last : current;
        return (Math.max(1, ping));
    }

    public void sendPing() {
        if (player.getScoreboardTags().contains("bot")) return;
        int packetId = random.nextInt(1, 10000);
        timeline.put(packetId, System.currentTimeMillis());
        WrapperPlayServerPing packet = new WrapperPlayServerPing(packetId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public boolean isOnGround(double verticalVelocity) {
        Material material = player.getLocation().getBlock().getType();
        if (player.isGliding() || material == Material.WATER || material == Material.LAVA
                || material == Material.COBWEB || material == Material.SCAFFOLDING)
            return false;

        double gDist = getDistanceToGround();
        if (gDist <= 0) return true;

        int tMax = verticalVelocity > 0 ? com.combatium.util.ping.PingCalculations.calculateTimeToMaxVelocity(verticalVelocity, gravity) : 0;
        double mH = verticalVelocity > 0 ? com.combatium.util.ping.PingCalculations.calculateDistanceTraveled(verticalVelocity, tMax, gravity) : 0;
        int tFall = com.combatium.util.ping.PingCalculations.calculateFallTime(verticalVelocity, mH + gDist, gravity);

        if (tFall == -1) return false;

        return getEstimatedPing() >= tMax + tFall / 20.0 * 1000 && gDist <= 1.3;
    }

    public double getDistanceToGround() {
        double collisionDist = 5;
        World world = player.getWorld();
        for (Location corner : getBBCorners()) {
            RayTraceResult result = world.rayTraceBlocks(corner, new Vector(0, -1, 0), 5, FluidCollisionMode.NEVER, true);
            if (result == null || result.getHitBlock() == null)
                continue;
            collisionDist = Math.min(collisionDist, corner.getY() - result.getHitBlock().getY());
        }
        return collisionDist - 1;
    }

    public Location @NotNull [] getBBCorners() {
        BoundingBox boundingBox = player.getBoundingBox();
        Location location = player.getLocation();
        World world = location.getWorld();
        double adjustment = 0.01; // To ensure the bounding box isn't clipping inside a wall
        return new Location[] {
                new Location(world, boundingBox.getMinX() + adjustment, location.getY(), boundingBox.getMinZ() + adjustment),
                new Location(world, boundingBox.getMinX() + adjustment, location.getY(), boundingBox.getMaxZ() - adjustment),
                new Location(world, boundingBox.getMaxX() - adjustment, location.getY(), boundingBox.getMinZ() + adjustment),
                new Location(world, boundingBox.getMaxX() - adjustment, location.getY(), boundingBox.getMaxZ() - adjustment)
        };
    }
}
