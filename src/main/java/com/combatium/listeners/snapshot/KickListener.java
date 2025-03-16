package com.combatium.listeners.snapshot;

import com.combatium.CombatiumLite;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import io.github.retrooper.packetevents.util.viaversion.ViaVersionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;

public class KickListener implements PacketListener {
    @Override
    public void onUserLogin(UserLoginEvent event) {
        FileConfiguration config = CombatiumLite.getInstance().getConfig();
        boolean shouldKick = config.getBoolean("snapshot.kick_enabled", false);

        Player player = event.getPlayer();

        int protocolVersion = ViaVersionUtil.getProtocolVersion(player);
        if (protocolVersion == 803 && shouldKick) {
            Component component = config.getComponent("snapshot.kick_message", MiniMessage.miniMessage(), Component.empty());
            player.kick(component, PlayerKickEvent.Cause.PLUGIN);
        }
    }
}
