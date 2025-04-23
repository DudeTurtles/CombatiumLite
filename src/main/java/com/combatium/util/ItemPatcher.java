package com.combatium.util;

import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.UseCooldown;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.bukkit.craftbukkit.inventory.CraftItemMetas;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
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
            applyDefaultComponents(useDuration(1), Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP,
                    Items.SUSPICIOUS_STEW, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.MILK_BUCKET, Items.HONEY_BOTTLE, Items.OMINOUS_BOTTLE);
            applyDefaultComponents(size(16).andThen(useDuration(1)), Items.POTION);
            applyDefaultComponents(size(64).andThen(cooldown(0.2F)), Items.SNOWBALL);
            applyDefaultComponents(cooldown(0.2F), Items.EGG);

            /*
            public static final Set<Material> Tools = Collections.unmodifiableSet(EnumSet.of(Material.TRIDENT,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.GOLDEN_AXE, Material.IRON_AXE,
            Material.DIAMOND_AXE, Material.NETHERITE_AXE, Material.WOODEN_SWORD, Material.STONE_SWORD,
            Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.NETHERITE_SWORD, Material.WOODEN_HOE,
            Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE, Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL, Material.GOLDEN_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE, Material.MACE));
             */

            // Add weapons / drinking time
            applyDefaultComponents(weapon(1.0F, 0.5F, 0.5F), Items.WOODEN_SWORD);
            applyDefaultComponents(weapon(1.0F, 0.5F, 0.5F), Items.GOLDEN_SWORD);
            applyDefaultComponents(weapon(2.0F, 0.5F, 0.5F), Items.STONE_SWORD);
            applyDefaultComponents(weapon(3.0F, 0.5F, 0.5F), Items.IRON_SWORD);
            applyDefaultComponents(weapon(4.0F, 0.5F, 0.5F), Items.DIAMOND_SWORD);
            applyDefaultComponents(weapon(5.0F, 0.5F, 0.5F), Items.NETHERITE_SWORD);

            applyDefaultComponents(weapon(3.0F, -0.5F, 0.0F), Items.WOODEN_AXE);
            applyDefaultComponents(weapon(3.0F, -0.5F, 0.0F), Items.GOLDEN_AXE);
            applyDefaultComponents(weapon(3.0F, -0.5F, 0.0F), Items.STONE_AXE);
            applyDefaultComponents(weapon(4.0F, -0.5F, 0.0F), Items.IRON_AXE);
            applyDefaultComponents(weapon(5.0F, -0.5F, 0.0F), Items.DIAMOND_AXE);
            applyDefaultComponents(weapon(6.0F, -0.5F, 0.0F), Items.NETHERITE_AXE);

            applyDefaultComponents(weapon(0.0F, -0.5F, 1.0F), Items.WOODEN_HOE);
            applyDefaultComponents(weapon(0.0F, 1.0F, 1.0F), Items.GOLDEN_HOE);
            applyDefaultComponents(weapon(0.0F, 0.0F, 1.0F), Items.STONE_HOE);
            applyDefaultComponents(weapon(1.0F, 0.5F, 1.0F), Items.IRON_HOE);
            applyDefaultComponents(weapon(1.0F, 1.0F, 1.0F), Items.DIAMOND_HOE);
            applyDefaultComponents(weapon(2.0F, 1.0F, 1.0F), Items.NETHERITE_HOE);

            applyDefaultComponents(weapon(0.0F, -0.5F, 0.0F), Items.WOODEN_SHOVEL);
            applyDefaultComponents(weapon(0.0F, -0.5F, 0.0F), Items.GOLDEN_SHOVEL);
            applyDefaultComponents(weapon(0.0F, -0.5F, 0.0F), Items.STONE_SHOVEL);
            applyDefaultComponents(weapon(1.0F, -0.5F, 0.0F), Items.IRON_SHOVEL);
            applyDefaultComponents(weapon(2.0F, -0.5F, 0.0F), Items.DIAMOND_SHOVEL);
            applyDefaultComponents(weapon(3.0F, -0.5F, 0.0F), Items.NETHERITE_SHOVEL);

            applyDefaultComponents(weapon(1.0F, 0.0F, 0.0F), Items.WOODEN_PICKAXE);
            applyDefaultComponents(weapon(1.0F, 0.0F, 0.0F), Items.GOLDEN_PICKAXE);
            applyDefaultComponents(weapon(1.0F, 0.0F, 0.0F), Items.STONE_PICKAXE);
            applyDefaultComponents(weapon(2.0F, 0.0F, 0.0F), Items.IRON_PICKAXE);
            applyDefaultComponents(weapon(3.0F, 0.0F, 0.0F), Items.DIAMOND_PICKAXE);
            applyDefaultComponents(weapon(4.0F, 0.0F, 0.0F), Items.NETHERITE_PICKAXE);

            applyDefaultComponents(weapon(5.0F, -0.5F, 1.0F), Items.TRIDENT);
            applyDefaultComponents(weapon(4.0F, -1.50F, 0.0F), Items.MACE);

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

        Field metaData = CraftItemType.class.getDeclaredField("itemMetaData");
        metaData.setAccessible(true);
        for (Item item : items) {
            PatchedDataComponentMap newMap = new PatchedDataComponentMap(item.components());
            patcher.accept(newMap);
            components.set(item, newMap);

            if (CraftItemType.minecraftToBukkitNew(item) instanceof CraftItemType<?> craftItemType) {
                metaData.set(craftItemType, Suppliers.memoize(() -> generate(CraftItemMetas.getItemMetaData(craftItemType))));
            }
        }
    }
    public static Consumer<PatchedDataComponentMap> useDuration(float useSeconds) {
        return patchedDataComponentMap -> {
            Consumable consumable = patchedDataComponentMap.get(DataComponents.CONSUMABLE);
            if (consumable != null) {
                Consumable newConsumable = new Consumable(useSeconds, consumable.animation(), consumable.sound(), consumable.hasConsumeParticles(), consumable.onConsumeEffects());
                patchedDataComponentMap.set(DataComponents.CONSUMABLE, newConsumable);
            }
        };
    }
    public static Consumer<PatchedDataComponentMap> weapon(float damage, float speed, float reach) {
        return patchedDataComponentMap -> {
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_CTS_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            if (reach != 0) builder.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ATTACK_REACH_ID, reach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            patchedDataComponentMap.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        };
    }
    public static Consumer<PatchedDataComponentMap> size(int stackSize) {
        return patchedDataComponentMap -> patchedDataComponentMap.set(DataComponents.MAX_STACK_SIZE, stackSize);
    }
    public static Consumer<PatchedDataComponentMap> cooldown(float seconds) {
        return patchedDataComponentMap -> patchedDataComponentMap.set(DataComponents.USE_COOLDOWN, new UseCooldown(seconds));
    }
    public static void addAttributeTooltips(Consumer<net.minecraft.network.chat.Component> tooltipAdder, ItemStack stack, @Nullable ServerPlayer player) {
        ItemAttributeModifiers itemAttributeModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (itemAttributeModifiers.showInTooltip()) {
            for (net.minecraft.world.entity.EquipmentSlotGroup equipmentSlotGroup : net.minecraft.world.entity.EquipmentSlotGroup.values()) {
                MutableBoolean mutableBoolean = new MutableBoolean(true);
                stack.forEachModifier(equipmentSlotGroup, (attribute, modifier) -> {
                    if (mutableBoolean.isTrue()) {
                        tooltipAdder.accept(CommonComponents.EMPTY);
                        tooltipAdder.accept(net.minecraft.network.chat.Component.translatable("item.modifiers." + equipmentSlotGroup.getSerializedName()).withStyle(ChatFormatting.GRAY).withStyle(Style.EMPTY.withItalic(false)));
                        mutableBoolean.setFalse();
                    }

                    addModifierTooltip(tooltipAdder, player, attribute, modifier);
                });
            }
        }
    }

    private static void addModifierTooltip(Consumer<net.minecraft.network.chat.Component> tooltipAdder, @Nullable ServerPlayer player, Holder<Attribute> attribute, AttributeModifier modifier) {
        double amount = modifier.amount();
        boolean flag = false;
        if (player != null) {
            if (modifier.is(Item.BASE_ATTACK_DAMAGE_ID)) {
                amount += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                flag = true;
            } else if (modifier.is(ItemPatcher.BASE_ATTACK_SPEED_CTS_ID)) {
                amount += player.getAttributeBaseValue(Attributes.ATTACK_SPEED) - 1.5;
                flag = true;
            } else if (modifier.is(ItemPatcher.BASE_ATTACK_REACH_ID)) {
                amount += player.getAttributeBaseValue(Attributes.ENTITY_INTERACTION_RANGE);
                flag = true;
            }
        }

        double d;
        if (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            d = amount * 100.0;
        } else if (attribute.is(Attributes.KNOCKBACK_RESISTANCE)) {
            d = amount * 10.0;
        } else {
            d = amount;
        }
        net.minecraft.network.chat.Component attributeComponent = net.minecraft.network.chat.Component.translatable(attribute.value().getDescriptionId());
        if (attribute.is(Attributes.ENTITY_INTERACTION_RANGE)) attributeComponent = net.minecraft.network.chat.Component.translatableWithFallback("attribute.name.attack_reach", "Attack Reach");

        if (flag) {
            tooltipAdder.accept(
                    CommonComponents.space()
                            .append(
                                    net.minecraft.network.chat.Component.translatable(
                                            "attribute.modifier.equals." + modifier.operation().id(),
                                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d),
                                            attributeComponent
                                    )
                            )
                            .withStyle(ChatFormatting.DARK_GREEN)
                            .withStyle(Style.EMPTY.withItalic(false))
            );
        } else if (amount > 0.0) {
            tooltipAdder.accept(
                    net.minecraft.network.chat.Component.translatable(
                                    "attribute.modifier.plus." + modifier.operation().id(),
                                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d),
                                    attributeComponent
                            )
                            .withStyle(attribute.value().getStyle(true))
                            .withStyle(Style.EMPTY.withItalic(false))
            );
        } else if (amount < 0.0) {
            tooltipAdder.accept(
                    net.minecraft.network.chat.Component.translatable(
                                    "attribute.modifier.take." + modifier.operation().id(),
                                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-d),
                                    net.minecraft.network.chat.Component.translatable(attribute.value().getDescriptionId())
                            )
                            .withStyle(attribute.value().getStyle(false))
                            .withStyle(Style.EMPTY.withItalic(false))
            );
        }
    }
}
