package com.combatium.util.ping;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PingManager {
    private static final Map<UUID, PlayerData> playerData = new HashMap<>();
    /*
     * Will be replaced with more robust ping calculations
     */
    public static int getPingTicks (Player player) {
        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) return 0;
        return (int) Math.min(5, (data.getEstimatedPing() / 50));
    }

    public static boolean hasPlayerData(UUID player) {
        return playerData.containsKey(player);
    }

    public static PlayerData getPlayerData(UUID player) {
        return playerData.get(player);
    }

    public static void addPlayerData(UUID player, PlayerData data) {
        playerData.put(player, data);
    }

    public static void removePlayerData(UUID player) {
        playerData.remove(player);
    }

    public static ArrayList<UUID> getPlayers() {
        return new ArrayList<>(playerData.keySet());
    }
}