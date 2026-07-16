package com.dudeturtles.combatium.listeners.snapshot;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.util.active.SweepingEdgeUtil;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AirSwingListener implements PacketListener {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        InteractPacketWrapper packet = new InteractPacketWrapper(event);
        if (packet.getAction() != InteractPacketWrapper.InteractAction.AIR_SWING) return;
        Player player = event.getPlayer();
        if (CombatiumLite.sweepOnMiss && CombatiumLite.getReachUtil().isOverCharged(player, 1.0F)
                && SweepingEdgeUtil.hasSweeping(player)) {
            SweepingEdgeUtil.sweepAttack(player, null);
        }
        ServerPlayer nmsAttacker = ((CraftPlayer)player).getHandle();

        nmsAttacker.postPiercingAttack();
        CombatiumLite.getReachUtil().resetCooldown(player, true);
        event.setCancelled(true);
    }
}
