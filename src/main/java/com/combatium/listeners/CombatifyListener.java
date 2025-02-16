package com.combatium.listeners;

import com.combatium.CombatiumLite;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CombatifyListener implements PacketListener {
    public static final List<UUID> combatifyPlayers = new ArrayList<>();

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            WrapperPlayClientPluginMessage packet = new WrapperPlayClientPluginMessage(event);
            if (packet.getChannelName().equals("minecraft:register") && new String(packet.getData()).contains("combatify:remaining_use_ticks")) {
                Player player = event.getPlayer();
                CombatiumLite.logger.info(player.getName() + " has joined with Combatify");
                combatifyPlayers.add(player.getUniqueId());
            }
        }
    }
}
