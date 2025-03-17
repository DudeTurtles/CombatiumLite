package com.combatium.listeners.util.ping;
import com.combatium.CombatiumLite;
import com.combatium.util.ping.PingManager;
import com.combatium.util.ping.PlayerPingData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.UUID;

public class JoinQuitListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (CombatiumLite.pingImprovements) PingManager.addPlayerData(player.getUniqueId(), new PlayerPingData(player));
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerPingData playerPingData = PingManager.getPlayerData(uuid);
        if (playerPingData == null)
            return;
        PingManager.removePlayerData(uuid);
    }
}