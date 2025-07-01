package com.combatium.listeners.snapshot;

import com.combatium.CombatiumLite;
import com.combatium.util.active.SweepingEdgeUtil;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.entity.Player;

public class AirSwingListener implements PacketListener {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        InteractPacketWrapper packet = new InteractPacketWrapper(event);
        if (packet.getAction() != InteractPacketWrapper.InteractAction.AIR_SWING) return;
        Player player = event.getPlayer();
        CombatiumLite.getReachUtil().resetCooldown(player, true);

        if (CombatiumLite.getReachUtil().isOverCharged(player, 0.0F)
                && (SweepingEdgeUtil.getSweepingLevel(player) > 0)) {
            SweepingEdgeUtil.Sweep(player, null);
        }

        event.setCancelled(true);
    }
}
