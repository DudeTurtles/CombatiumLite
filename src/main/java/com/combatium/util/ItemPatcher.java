package com.combatium.util;

import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.UseCooldown;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.craftbukkit.inventory.CraftItemMetas;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;

import static com.combatium.CombatiumLite.logger;

public class ItemPatcher {
    public static final ResourceLocation BASE_ATTACK_SPEED_CTS_ID = ResourceLocation.withDefaultNamespace("base_attack_speed_cts");
    public static final ResourceLocation BASE_ATTACK_REACH_ID = ResourceLocation.withDefaultNamespace("base_attack_reach");
    public static final TriFunction<CraftItemMetas.ItemMetaData<?>, ItemStack, Set<DataComponentType<?>>, ?> WRAPPER = (meta, stack, extra) -> {
        if (!stack.isEmpty()) {
            DataComponentMap prototype = stack.getPrototype();
            if (prototype instanceof PatchedDataComponentMap prototypeDataComponentMap) {
                ItemStack newStack = stack.copy();
                if (newStack.getComponents() instanceof PatchedDataComponentMap patchedDataComponentMap) {
                    patchedDataComponentMap.restorePatch(prototypeDataComponentMap.asPatch());
                    patchedDataComponentMap.applyPatch(stack.getComponentsPatch());
                }
                stack = newStack;
            }
        }
        return meta.fromItemStack().apply(stack, extra);
    };

    public static void patch() {
        try {
            // Patch components here
            applyDefaultComponents(size(16), Items.POTION);
            applyDefaultComponents(size(64).andThen(cooldown(0.2F)), Items.SNOWBALL);
            applyDefaultComponents(cooldown(0.2F), Items.EGG);
            applyDefaultComponents(weapon(4.0F, 0.5F, 0.5F), Items.DIAMOND_SWORD);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.log(Level.SEVERE, "Failed to update default components!" + e);
        }
    }

    public static void applyDefaultComponents(Consumer<PatchedDataComponentMap> patcher, Item item) throws NoSuchFieldException, IllegalAccessException {
        Field components = Item.class.getDeclaredField("components");
        components.setAccessible(true);

        PatchedDataComponentMap newMap = new PatchedDataComponentMap(item.components());
        patcher.accept(newMap);
        components.set(item, newMap);

        if (CraftItemType.minecraftToBukkitNew(item) instanceof CraftItemType<?> craftItemType) {
            Field metaData = CraftItemType.class.getDeclaredField("itemMetaData");
            metaData.setAccessible(true);
            metaData.set(craftItemType, Suppliers.memoize(() -> generate(CraftItemMetas.getItemMetaData(craftItemType))));
        }
    }
    public static <I extends ItemMeta> CraftItemMetas.ItemMetaData<I> generate(CraftItemMetas.ItemMetaData<I> meta) {
        return new CraftItemMetas.ItemMetaData<>(meta.metaClass(), (stack, extra) -> (I) WRAPPER.apply(meta, stack, extra), meta.fromItemMeta());
    }
    public static void applyDefaultComponents(Consumer<PatchedDataComponentMap> patcher, Item... items) throws NoSuchFieldException, IllegalAccessException {
        Field components = Item.class.getDeclaredField("components");
        components.setAccessible(true);

        for (Item item : items) {
            PatchedDataComponentMap newMap = new PatchedDataComponentMap(item.components());
            patcher.accept(newMap);
            components.set(item, newMap);

            if (CraftItemType.minecraftToBukkitNew(item) instanceof CraftItemType<?> craftItemType) {
                Field metaData = CraftItemType.class.getDeclaredField("itemMetaData");
                metaData.setAccessible(true);
                metaData.set(craftItemType, Suppliers.memoize(() -> generate(CraftItemMetas.getItemMetaData(craftItemType))));
            }
        }
    }
    public static Consumer<PatchedDataComponentMap> weapon(float damage, float speed, float reach) {
        return patchedDataComponentMap -> {
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_CTS_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            builder.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ATTACK_REACH_ID, reach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            patchedDataComponentMap.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build().withTooltip(false));
            List<Component> lore = new ArrayList<>();
            Style commonStyle = CommonComponents.space().getStyle().withItalic(false).withColor(ChatFormatting.GRAY);
            lore.add(CommonComponents.space());
            lore.add(Component.literal("When in Main Hand:").setStyle(commonStyle));
            lore.add(CommonComponents.space().append(Component.literal(format(damage + 2) + " Attack Damage").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));
            lore.add(CommonComponents.space().append(Component.literal(format(speed + 2.5) + " Attack Speed").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));
            lore.add(CommonComponents.space().setStyle(Style.EMPTY).append(Component.literal(format(reach + 2.5) + " Attack Reach").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));

            patchedDataComponentMap.set(DataComponents.LORE, new ItemLore(lore));
        };
    }
    public static Consumer<PatchedDataComponentMap> size(int stackSize) {
        return patchedDataComponentMap -> patchedDataComponentMap.set(DataComponents.MAX_STACK_SIZE, stackSize);
    }
    public static Consumer<PatchedDataComponentMap> cooldown(float seconds) {
        return patchedDataComponentMap -> patchedDataComponentMap.set(DataComponents.USE_COOLDOWN, new UseCooldown(seconds));
    }
    private static String format(double value) {
        DecimalFormat df = new DecimalFormat("0.#");
        return df.format(value);
    }
}
