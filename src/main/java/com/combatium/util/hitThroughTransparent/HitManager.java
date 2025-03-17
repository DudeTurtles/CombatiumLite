package com.combatium.util.hitThroughTransparent;

import com.combatium.util.active.ReachUtil;
import com.combatium.util.ping.PingManager;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;

import static com.combatium.util.calculations.ValidHitChecks.isValidHit;


public class HitManager {
    private static final Map<Player, PlayerData> data = new HashMap<>();

    public static void updatePlayerData(Player player) {
        if (!data.containsKey(player)) {
            data.put(player, new PlayerData(player));
        } else {
            data.get(player).update();
        }
    }

    public static void removePlayerData(Player player) {
        data.remove(player);
    }

    public static BoundingBox getBoundingBox(Player player, int ticks) {
        return data.get(player).getNearestBox(ticks);
    }

    public static LivingEntity checkHit(Player player) {
        int ticks = PingManager.getPingTicks(player);
        PlayerData playerData = data.get(player);
        Location eyeLocation = playerData.getNearestEyeLocation(ticks);
        Vector direction = eyeLocation.getDirection();

        // Check entities that may be hit
        World world = player.getWorld();
        RayTraceResult result = RayTraceUtil.rayTrace(world, eyeLocation, direction, ReachUtil.getReach(player) + 1, FluidCollisionMode.NEVER, true, 0, entity -> entity instanceof LivingEntity && entity != player, ticks);

        if (result != null && result.getHitEntity() != null && result.getHitEntity() instanceof LivingEntity entity &&
                isValidHit(player, entity, 0.5)) return entity;
        return null;
    }
}
