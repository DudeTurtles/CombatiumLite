package com.combatium.listeners.util.ping;

import com.combatium.CombatiumLite;
import com.combatium.util.ping.PingManager;
import com.combatium.util.ping.PlayerPingData;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import org.bukkit.entity.Player;

public class PingReceiveListener extends PacketListenerAbstract {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!CombatiumLite.pingImprovements) return;
        if (event.getPacketType() != PacketType.Play.Client.PONG) return;
        Player player = event.getPlayer();
        PlayerPingData playerPingData = PingManager.getPlayerData(player.getUniqueId());
        // If player is timed out by the server and removed from the map on the main thread
        // The server can still receive ping packets from the disconnected client
        // At which point the entry will no longer be in the map but this code will be processed!
        if (playerPingData == null) return;
        int packetId = new WrapperPlayClientPong(event).getId();
        Long sendTime = playerPingData.getTimeline().get(packetId);
        if (sendTime == null)
            return;
        long ping = System.currentTimeMillis() - sendTime;
        playerPingData.getTimeline().remove(packetId);
        playerPingData.setLastPing((playerPingData.getPing() != null) ? playerPingData.getPing() : ping);
        playerPingData.setPing(ping);
    }
}