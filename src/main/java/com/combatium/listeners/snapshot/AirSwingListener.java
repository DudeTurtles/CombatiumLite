package com.combatium.listeners.snapshot;

import com.combatium.util.active.ReachUtil;
import com.combatium.util.active.SweepingEdgeUtil;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AirSwingListener implements PacketListener {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        InteractPacketWrapper packet = new InteractPacketWrapper(event);
        if (packet.getAction() == InteractPacketWrapper.InteractAction.AIR_SWING) {
            Player player = event.getPlayer();
            ReachUtil.resetCooldown(player, true, new ItemStack(Material.AIR));

            if (ReachUtil.isOverCharged(player, player.getInventory().getItemInMainHand())
                    && (SweepingEdgeUtil.getSweepingLevel(player) > 0)) {
                SweepingEdgeUtil.Sweep(player, null);
            }

            event.setCancelled(true);
        }
    }
}
