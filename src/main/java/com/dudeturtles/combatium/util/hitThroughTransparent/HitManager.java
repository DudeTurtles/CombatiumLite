package com.dudeturtles.combatium.util.hitThroughTransparent;

import com.dudeturtles.combatium.util.ping.PingManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;
import java.util.Map;

import static com.dudeturtles.combatium.util.calculations.ValidHitChecks.isValidHit;


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
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

        // Check entities that may be hit
        HitResult result = RayTraceUtil.raycastHitResult(nmsPlayer, nmsPlayer.getCamera(), eyeLocation);

        if (result instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity entity &&
                isValidHit(player, entity, 0.5)) return entity;
        return null;
    }
}
