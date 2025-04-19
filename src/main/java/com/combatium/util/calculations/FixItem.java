package com.combatium.util.calculations;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.List;

import static com.combatium.util.active.PlayerDamageEntity.*;


public class FixItem {
    public static final NamespacedKey UPDATE_LORE = new NamespacedKey("combatium", "update_lore");
    private static final java.lang.invoke.VarHandle API_ITEM_STACK_CRAFT_DELEGATE_FIELD;

    static {
        try {
            API_ITEM_STACK_CRAFT_DELEGATE_FIELD = java.lang.invoke.MethodHandles.privateLookupIn(
                    ItemStack.class,
                    java.lang.invoke.MethodHandles.lookup()
            ).findVarHandle(ItemStack.class, "craftDelegate", ItemStack.class);
        } catch (final IllegalAccessException | NoSuchFieldException exception) {
            throw new RuntimeException(exception);
        }
    }

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

    public static ItemMeta updateLore(ItemStack item) {
        //Checks if an items lore needs to be updated
        if (item == null || item.getItemMeta() == null) return null;

        if (item.getItemMeta().getPersistentDataContainer().has(UPDATE_LORE, PersistentDataType.BOOLEAN)) return null;

        net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.fromBukkitCopy(item);
        ItemLore itemLore = nmsItem.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
        List<net.minecraft.network.chat.Component> nmsLore = new ArrayList<>(itemLore.lines());

        if (nmsLore.isEmpty()) return null;

        nmsLore.set(0, nmsLore.getFirst().copy().append(CommonComponents.space()));
        nmsItem.set(DataComponents.LORE, new ItemLore(nmsLore, itemLore.styledLines()));
        nmsItem.set(DataComponents.ATTRIBUTE_MODIFIERS, nmsItem.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).withTooltip(false).withModifierAdded(Attributes.ATTACK_DAMAGE, new net.minecraft.world.entity.ai.attributes.AttributeModifier(ResourceLocation.fromNamespaceAndPath("combatium", "useless"), 0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND));

        ItemStack newItem = CraftItemStack.asBukkitCopy(nmsItem);
        ItemMeta meta = newItem.getItemMeta();
        if (meta.getPersistentDataContainer().has(UPDATE_LORE, PersistentDataType.BOOLEAN)) {
            return null;
        }

        meta.getPersistentDataContainer().set(UPDATE_LORE, PersistentDataType.BOOLEAN, true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        return meta;
    }

    public static boolean isSword(Material item) {
        return switch (item) {
            case WOODEN_SWORD, STONE_SWORD, GOLDEN_SWORD, IRON_SWORD, DIAMOND_SWORD, NETHERITE_SWORD -> true;
            default -> false;
        };
    }
}
