package com.dudeturtles.combatium.listeners.damage;

import com.dudeturtles.combatium.CombatiumLite;
import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import static com.dudeturtles.combatium.util.calculations.DisableShieldUtil.getCleavingLevel;

public class PlayerShieldDisableListener implements Listener {
    public static void disable(Entity entity, Player target, ItemStack blockingItem, BlocksAttacks blocksAttacks, int ticks) {
        ServerLevel serverLevel = entity.level().getMinecraftWorld();

        int cleavingLevel = 0;
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack itemStack = livingEntity.getMainHandItem();
            cleavingLevel = getCleavingLevel(itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY), serverLevel.registryAccess());
        }

        CombatiumLite.getPluginLogger().info("Cleaving Level: " + cleavingLevel);
        if (cleavingLevel > 0) {
            ticks += 10 * cleavingLevel;
        }
        if (ticks <= 0) return;
        target.getCooldowns().addCooldown(blockingItem, ticks);

        target.stopUsingItem();
        blocksAttacks.disableSound().ifPresent((holder) -> serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), holder, target.getSoundSource(), 0.8F, 0.8F + serverLevel.getRandom().nextFloat() * 0.4F));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void checkDisable(PlayerShieldDisableEvent event) {
        Player player = ((CraftPlayer) event.getPlayer()).getHandle();
        ItemStack blockingItem = player.getItemBlockingWith();
        BlocksAttacks blocksAttacks = blockingItem == null ? null : blockingItem.get(DataComponents.BLOCKS_ATTACKS);

        if (blocksAttacks == null) return;

        disable(((CraftEntity) event.getDamager()).getHandle(), player, blockingItem, blocksAttacks, event.getCooldown());
        event.setCancelled(true);
    }
}