package com.combatium.util.ping;

import com.combatium.CombatiumLite;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;

public class PingRunnable extends BukkitRunnable {
    @Override
    public void run() {
        if (!CombatiumLite.pingImprovements) return;
        for (UUID uuid : PingManager.getPlayers()) {
            PlayerData playerData = PingManager.getPlayerData(uuid);
            playerData.sendPing();
        }
    }
}