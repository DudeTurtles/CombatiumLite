package com.dudeturtles.combatium.listeners.snapshot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

public class DefaultedListener implements Listener {
    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event) {
//        if (event.getChannel().equals(ClientboundDefaultComponentsSyncPacket.TYPE.id().toString())) {
//            RegistryFriendlyByteBuf registryFriendlyByteBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), ((CraftPlayer)event.getPlayer()).getHandle().registryAccess());
//            ClientboundDefaultComponentsSyncPacket.CODEC.encode(registryFriendlyByteBuf, new ClientboundDefaultComponentsSyncPacket((ArrayList<ItemPatches>) ItemPatcher.DEFAULT_PATCHES));
//            event.getPlayer().sendPluginMessage(CombatiumLite.getInstance(), ClientboundDefaultComponentsSyncPacket.TYPE.id().toString(), Arrays.copyOf(registryFriendlyByteBuf.array(), registryFriendlyByteBuf.writerIndex()));
//        }
    }
}
