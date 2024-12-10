package com.combatium.listeners.util.ping;
import com.combatium.util.ping.PingManager;
import com.combatium.util.ping.PlayerData;
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
        PingManager.addPlayerData(player.getUniqueId(), new PlayerData(player));
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerData playerData = PingManager.getPlayerData(uuid);
        if (playerData == null)
            return;
        PingManager.removePlayerData(uuid);
    }
}