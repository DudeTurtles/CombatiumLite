package com.combatium.util;

import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.UseCooldown;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

import static com.combatium.CombatiumLite.logger;

public class ItemPatcher {
    public static final ResourceLocation BASE_ATTACK_SPEED_CTS_ID = ResourceLocation.withDefaultNamespace("base_attack_speed_cts");
    public static final ResourceLocation BASE_ATTACK_REACH_ID = ResourceLocation.withDefaultNamespace("base_attack_reach");

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
    }
    public static void applyDefaultComponents(Consumer<PatchedDataComponentMap> patcher, Item... items) throws NoSuchFieldException, IllegalAccessException {
        Field components = Item.class.getDeclaredField("components");
        components.setAccessible(true);

        for (Item item : items) {
            PatchedDataComponentMap newMap = new PatchedDataComponentMap(item.components());
            patcher.accept(newMap);
            components.set(item, newMap);
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
            lore.add(CommonComponents.space().append(Component.literal((damage + 2) + " Attack Damage").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));
            lore.add(CommonComponents.space().append(Component.literal((speed + 2.5) + " Attack Speed").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));
            lore.add(CommonComponents.space().setStyle(Style.EMPTY).append(Component.literal((reach + 2.5) + " Attack Reach").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));

            patchedDataComponentMap.set(DataComponents.LORE, new ItemLore(lore));
        };
    }
    public static Consumer<PatchedDataComponentMap> size(int stackSize) {
        return patchedDataComponentMap -> patchedDataComponentMap.set(DataComponents.MAX_STACK_SIZE, stackSize);
    }
    public static Consumer<PatchedDataComponentMap> cooldown(float seconds) {
        return patchedDataComponentMap -> patchedDataComponentMap.set(DataComponents.USE_COOLDOWN, new UseCooldown(seconds));
    }
}
