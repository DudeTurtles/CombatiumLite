package com.dudeturtles.combatium.listeners.snapshot;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.util.active.SweepingEdgeUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CombatifyListener implements Listener, PluginMessageListener {
    public static final List<UUID> combatifyPlayers = new ArrayList<>();

    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event) {
        if (!event.getChannel().equals("combatify:remaining_use_ticks")) return;
        Player player = event.getPlayer();
        CombatiumLite.getPluginLogger().info(player.getName() + " has joined with Combatify");
        combatifyPlayers.add(player.getUniqueId());
        net.minecraft.world.entity.player.Player nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.needsSync = true;
        try {
            Method setDirty = AttributeInstance.class.getDeclaredMethod("setDirty");
            setDirty.setAccessible(true);
            setDirty.invoke(nmsPlayer.getAttribute(Attributes.ATTACK_SPEED));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {}
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        combatifyPlayers.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] bytes) {
        if (!channel.equals("combatify:miss_attack")) return;
        //CombatiumLite.logger.info(player.getName() + " has missed");

        if (CombatiumLite.sweepOnMiss && CombatiumLite.getReachUtil().isOverCharged(player, 1.0F)
                && SweepingEdgeUtil.hasSweeping(player)) {
            SweepingEdgeUtil.sweepAttack(player, null);
        }
        ServerPlayer nmsAttacker = ((CraftPlayer)player).getHandle();

        nmsAttacker.lungeForwardMaybe();
        CombatiumLite.getReachUtil().resetCooldown(player, true);
        nmsAttacker.swing(nmsAttacker.swingingArm, true);
    }

    public static boolean isCombatifyPlayer(Player player) {
        return combatifyPlayers.contains(player.getUniqueId());
    }
}
