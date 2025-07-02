package com.dudeturtles.combatium.listeners.damage;

import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import static com.dudeturtles.combatium.util.calculations.DisableShieldUtil.getCleavingLevel;

public class PlayerShieldDisableListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void checkDisable(PlayerShieldDisableEvent event) {
        Player player = ((CraftPlayer) event.getPlayer()).getHandle();
        ItemStack blockingItem = player.getItemBlockingWith();
        BlocksAttacks blocksAttacks = blockingItem == null ? null : blockingItem.get(DataComponents.BLOCKS_ATTACKS);

        if (blocksAttacks == null) return;

        disable((ServerLevel) ((CraftEntity) event.getDamager()).getHandle().level(), player, blockingItem, blocksAttacks, event.getCooldown());
        event.setCancelled(true);

    }
    public static void disable(ServerLevel serverLevel, Player target, ItemStack item, BlocksAttacks blocksAttacks, int ticks) {
        int cleavingLevel = getCleavingLevel(item.getBukkitStack());
        if (cleavingLevel > 0) {
            ticks += 10 * cleavingLevel;
        }
        if (ticks <= 0) return;
        target.getCooldowns().addCooldown(item, ticks);

        target.stopUsingItem();
        blocksAttacks.disableSound().ifPresent((holder) -> serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), holder, target.getSoundSource(), 0.8F, 0.8F + serverLevel.random.nextFloat() * 0.4F));
    }
}
