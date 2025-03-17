package com.combatium.listeners.snapshot;

import com.combatium.CombatiumLite;
import com.combatium.util.active.ReachUtil;
import com.combatium.util.active.SweepingEdgeUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CombatifyListener implements Listener, PluginMessageListener {
    public static final List<UUID> combatifyPlayers = new ArrayList<>();

    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event) {
        if (event.getChannel().equals("combatify:remaining_use_ticks")) {
            Player player = event.getPlayer();
            CombatiumLite.logger.info(player.getName() + " has joined with Combatify");
            player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(4.0);
            combatifyPlayers.add(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        combatifyPlayers.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] bytes) {
        if (channel.equals("combatify:miss_attack")) {
            //CombatiumLite.logger.info(player.getName() + " has missed");

            ReachUtil.resetCooldown(player, true, player.getInventory().getItemInMainHand().getType());

            if (ReachUtil.isOverCharged(player, player.getInventory().getItemInMainHand().getType(), ReachUtil.getHaste(player))
                    && (SweepingEdgeUtil.getSweepingLevel(player) > 0)) {
                SweepingEdgeUtil.Sweep(player, null);
            }
        }
    }

    public static boolean isCombatifyPlayer(Player player) {
        return combatifyPlayers.contains(player.getUniqueId());
    }
}
