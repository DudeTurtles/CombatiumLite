package com.combatium.listeners.snapshot;

import com.combatium.util.active.ReachUtil;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Material;

public class AirSwingListener implements PacketListener {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        InteractPacketWrapper packet = new InteractPacketWrapper(event);
        if (packet.getAction() == InteractPacketWrapper.InteractAction.AIR_SWING) {
            ReachUtil.resetCooldown(event.getPlayer(), true, Material.AIR);
            event.setCancelled(true);
        }
    }
}
