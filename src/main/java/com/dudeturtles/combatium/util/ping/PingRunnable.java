package com.dudeturtles.combatium.util.ping;

import com.dudeturtles.combatium.CombatiumLite;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;

public class PingRunnable extends BukkitRunnable {
    @Override
    public void run() {
        if (!CombatiumLite.pingImprovements) return;
        for (UUID uuid : PingManager.getPlayers()) {
            PlayerPingData playerPingData = PingManager.getPlayerData(uuid);
            playerPingData.sendPing();
        }
    }
}