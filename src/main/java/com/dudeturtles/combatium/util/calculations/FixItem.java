package com.dudeturtles.combatium.util.calculations;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import static com.dudeturtles.combatium.util.active.PlayerDamageEntity.*;


public class FixItem {

    public static ItemStack makeSwordBlock(ItemStack item) {
        if (!isSword(item.getType())) return null;
        Consumable consumable = Consumable.builder().
                consumeSeconds(99999).
                hasConsumeParticles(false).
                animation(ItemUseAnimation.BLOCK).
                build();
        net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.fromBukkitCopy(item);
        DataComponentPatch test = DataComponentPatch.builder().set(DataComponents.CONSUMABLE, consumable).build();
        nmsItem.applyComponents(test);
        ItemStack bukkitItem = CraftItemStack.asBukkitCopy(nmsItem);
        ItemMeta meta = bukkitItem.getItemMeta();
        meta.getPersistentDataContainer().set(BLOCK_ANGLE, PersistentDataType.DOUBLE, 130.0);
        meta.getPersistentDataContainer().set(KNOCKBACK_REDUCTION, PersistentDataType.DOUBLE, 0.3);
        meta.getPersistentDataContainer().set(BLOCK_AMOUNT, PersistentDataType.DOUBLE, 3.0);
        bukkitItem.setItemMeta(meta);
        return bukkitItem;
    }

    public static boolean isSword(Material item) {
        return switch (item) {
            case WOODEN_SWORD, STONE_SWORD, GOLDEN_SWORD, IRON_SWORD, DIAMOND_SWORD, NETHERITE_SWORD -> true;
            default -> false;
        };
    }
}
