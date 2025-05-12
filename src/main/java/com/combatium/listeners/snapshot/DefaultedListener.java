package com.combatium.listeners.snapshot;

import com.combatium.CombatiumLite;
import com.combatium.util.ItemPatcher;
import io.netty.buffer.Unpooled;
import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class DefaultedListener implements Listener {
    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event) {
        if (event.getChannel().equals(ClientboundDefaultComponentsSyncPacket.TYPE.id().toString())) {
            RegistryFriendlyByteBuf registryFriendlyByteBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), ((CraftPlayer)event.getPlayer()).getHandle().registryAccess());
            ClientboundDefaultComponentsSyncPacket.CODEC.encode(registryFriendlyByteBuf, new ClientboundDefaultComponentsSyncPacket((ArrayList<ItemPatches>) ItemPatcher.DEFAULT_PATCHES));
            event.getPlayer().sendPluginMessage(CombatiumLite.getInstance(), ClientboundDefaultComponentsSyncPacket.TYPE.id().toString(), Arrays.copyOf(registryFriendlyByteBuf.array(), registryFriendlyByteBuf.writerIndex()));
        }
    }
}
