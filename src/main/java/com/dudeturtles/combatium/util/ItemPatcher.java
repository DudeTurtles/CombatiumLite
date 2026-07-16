package com.dudeturtles.combatium.util;

import com.dudeturtles.combatium.CombatiumLite;
import net.atlas.defaulted.component.ItemPatches;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.*;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.craftbukkit.inventory.CraftItemMetas;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;

public class ItemPatcher {
    public static final String MOD_ID = "combatium";
    public static final String CHARGED_REACH_KEY = "combatium_weapon_charged_reach";
    public static List<ItemPatches> DEFAULT_PATCHES = new ArrayList<>();
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
    public static final Identifier BASE_ATTACK_SPEED_CTS_ID = Identifier.withDefaultNamespace("base_attack_speed_cts");
    public static final Identifier BASE_ATTACK_REACH_ID = Identifier.withDefaultNamespace("base_attack_reach");
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
            Map<Identifier, ItemPatches> intermediary = new HashMap<>();
            // Patch components here
            applyDefaultComponents(useDuration(1), id("drinkables"), intermediary, Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP,
                    Items.SUSPICIOUS_STEW, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.MILK_BUCKET, Items.HONEY_BOTTLE, Items.OMINOUS_BOTTLE);
            applyDefaultComponents(size(16).andThen(useDuration(1)), intermediary, Items.POTION);
            applyDefaultComponents(size(64).andThen(cooldown(0.2F)), intermediary, Items.SNOWBALL);
            applyDefaultComponents(cooldown(0.2F), id("eggs"), intermediary, Items.EGG, Items.BROWN_EGG, Items.BLUE_EGG);

            applyDefaultComponents(blocksAttacks(true, true), intermediary, Items.SHIELD);

            applyDefaultComponents(weapon(2.0F, 0.5F, 0.5F), intermediary, Items.WOODEN_SWORD);
            applyDefaultComponents(weapon(2.0F, 0.5F, 0.5F), intermediary, Items.GOLDEN_SWORD);
            applyDefaultComponents(weapon(2.0F, 0.5F, 0.5F), intermediary, Items.STONE_SWORD);
            applyDefaultComponents(weapon(2.0F, 0.5F, 0.5F), intermediary, Items.COPPER_SWORD);
            applyDefaultComponents(weapon(3.0F, 0.5F, 0.5F), intermediary, Items.IRON_SWORD);
            applyDefaultComponents(weapon(4.0F, 0.5F, 0.5F), intermediary, Items.DIAMOND_SWORD);
            applyDefaultComponents(weapon(5.0F, 0.5F, 0.5F), intermediary, Items.NETHERITE_SWORD);

            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(3.0F, -0.5F, 0.0F)), intermediary, Items.WOODEN_AXE);
            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(3.0F, -0.5F, 0.0F)), intermediary, Items.GOLDEN_AXE);
            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(3.0F, -0.5F, 0.0F)), intermediary, Items.STONE_AXE);
            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(3.0F, -0.5F, 0.0F)), intermediary, Items.COPPER_AXE);
            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(4.0F, -0.5F, 0.0F)), intermediary, Items.IRON_AXE);
            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(5.0F, -0.5F, 0.0F)), intermediary, Items.DIAMOND_AXE);
            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(6.0F, -0.5F, 0.0F)), intermediary, Items.NETHERITE_AXE);

            applyDefaultComponents(weapon(0.0F, -0.5F, 1.0F), intermediary, Items.WOODEN_HOE);
            applyDefaultComponents(weapon(0.0F, 1.0F, 1.0F), intermediary, Items.GOLDEN_HOE);
            applyDefaultComponents(weapon(0.0F, 0.0F, 1.0F), intermediary, Items.STONE_HOE);
            applyDefaultComponents(weapon(0.0F, 0.0F, 1.0F), intermediary, Items.COPPER_HOE);
            applyDefaultComponents(weapon(1.0F, 0.5F, 1.0F), intermediary, Items.IRON_HOE);
            applyDefaultComponents(weapon(1.0F, 1.0F, 1.0F), intermediary, Items.DIAMOND_HOE);
            applyDefaultComponents(weapon(2.0F, 1.0F, 1.0F), intermediary, Items.NETHERITE_HOE);

            applyDefaultComponents(weapon(0.0F, -0.5F, 0.0F), intermediary, Items.WOODEN_SHOVEL);
            applyDefaultComponents(weapon(0.0F, -0.5F, 0.0F), intermediary, Items.GOLDEN_SHOVEL);
            applyDefaultComponents(weapon(0.0F, -0.5F, 0.0F), intermediary, Items.STONE_SHOVEL);
            applyDefaultComponents(weapon(0.0F, -0.5F, 0.0F), intermediary, Items.COPPER_SHOVEL);
            applyDefaultComponents(weapon(1.0F, -0.5F, 0.0F), intermediary, Items.IRON_SHOVEL);
            applyDefaultComponents(weapon(2.0F, -0.5F, 0.0F), intermediary, Items.DIAMOND_SHOVEL);
            applyDefaultComponents(weapon(3.0F, -0.5F, 0.0F), intermediary, Items.NETHERITE_SHOVEL);

            applyDefaultComponents(weapon(1.0F, 0.0F, 0.0F), intermediary, Items.WOODEN_PICKAXE);
            applyDefaultComponents(weapon(1.0F, 0.0F, 0.0F), intermediary, Items.GOLDEN_PICKAXE);
            applyDefaultComponents(weapon(1.0F, 0.0F, 0.0F), intermediary, Items.STONE_PICKAXE);
            applyDefaultComponents(weapon(1.0F, 0.0F, 0.0F), intermediary, Items.COPPER_PICKAXE);
            applyDefaultComponents(weapon(2.0F, 0.0F, 0.0F), intermediary, Items.IRON_PICKAXE);
            applyDefaultComponents(weapon(3.0F, 0.0F, 0.0F), intermediary, Items.DIAMOND_PICKAXE);
            applyDefaultComponents(weapon(4.0F, 0.0F, 0.0F), intermediary, Items.NETHERITE_PICKAXE);

            applyDefaultComponents(weapon(0.0F, 0.5F, 2.0F, 0.0F), intermediary, Items.WOODEN_SPEAR);
            applyDefaultComponents(weapon(0.0F, -0.25F, 2.0F, 0.0F), intermediary, Items.GOLDEN_SPEAR);
            applyDefaultComponents(weapon(0.0F, 0.25F, 2.0F, 0.0F), intermediary, Items.STONE_SPEAR);
            applyDefaultComponents(weapon(0.0F, 0.0F, 2.0F, 0.0F), intermediary, Items.COPPER_SPEAR);
            applyDefaultComponents(weapon(1.0F, -0.25F, 2.0F, 0.0F), intermediary, Items.IRON_SPEAR);
            applyDefaultComponents(weapon(2.0F, -0.5F, 2.0F, 0.0F), intermediary, Items.DIAMOND_SPEAR);
            applyDefaultComponents(weapon(3.0F, -0.75F, 2.0F, 0.0F), intermediary, Items.NETHERITE_SPEAR);

            applyDefaultComponents(weapon(5.0F, -0.5F, 1.0F), intermediary, Items.TRIDENT);
            applyDefaultComponents(weapon(4.0F, -1.50F, 0.0F), intermediary, Items.MACE);
            DEFAULT_PATCHES = new ArrayList<>(intermediary.entrySet().stream().map(entry -> new ItemPatchesEntry(entry.getKey(), entry.getValue())).sorted(Comparator.naturalOrder()).map(ItemPatchesEntry::itemPatches).toList());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            CombatiumLite.getPluginLogger().log(Level.SEVERE, "Failed to update default components!" + e);
        }
    }

    public static <T> void set(DataComponentMap.Builder builder, TypedDataComponent<T> typed) {
        builder.set(typed.type(), typed.value());
    }

    public static void applyDefaultComponents(BiConsumer<DataComponentTypeSetter, HolderLookup.Provider> patcher, Map<Identifier, ItemPatches> intermediary, Item item) throws NoSuchFieldException, IllegalAccessException {
        Holder.Reference<Item> itemHolder = item.builtInRegistryHolder();
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.add(itemHolder.key(),
                (builder, provider, resourceKey) -> {
                    PatchedDataComponentMap newMap = new PatchedDataComponentMap(builder.build());
                    patcher.accept(new DataComponentTypeSetter(newMap, builder), provider);
                    intermediary.put(id(itemHolder.key().identifier().getPath()), new ItemPatches(HolderSet.direct(itemHolder), Collections.emptyList(), Collections.emptyList(), newMap.asPatch(), 1000));
                });
    }
    public static <I extends ItemMeta> CraftItemMetas.ItemMetaData<I> generate(CraftItemMetas.ItemMetaData<I> meta) {
        return new CraftItemMetas.ItemMetaData<>(meta.metaClass(), (stack, extra) -> (I) WRAPPER.apply(meta, stack, extra), meta.fromItemMeta());
    }
    public static void applyDefaultComponents(BiConsumer<DataComponentTypeSetter, HolderLookup.Provider> patcher, Identifier patchName, Map<Identifier, ItemPatches> intermediary, Item... items) throws NoSuchFieldException, IllegalAccessException {
        for (Item item : items) {
            Holder.Reference<Item> itemHolder = item.builtInRegistryHolder();
            BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.add(itemHolder.key(),
                    (builder, provider, resourceKey) -> {
                        PatchedDataComponentMap newMap = new PatchedDataComponentMap(builder.build());
                        patcher.accept(new DataComponentTypeSetter(newMap, builder), provider);
                        intermediary.put(id(itemHolder.key().identifier().getPath()), new ItemPatches(HolderSet.direct(itemHolder), Collections.emptyList(), Collections.emptyList(), newMap.asPatch(), 1000));
                    });
        }
    }
    public static BiConsumer<DataComponentTypeSetter, HolderLookup.Provider> blocksAttacks(boolean negateProjectiles, boolean useShieldSounds) {
        return (setter, provider) -> {
            List<BlocksAttacks.DamageReduction> reductions = new ArrayList<>();
            reductions.add(new BlocksAttacks.DamageReduction((float) CombatiumLite.shieldProtectionArc, Optional.empty(), CombatiumLite.shieldBaseBlocking, CombatiumLite.shieldBlockingMultiplier));
            if (negateProjectiles) {
                reductions.add(new BlocksAttacks.DamageReduction((float) CombatiumLite.shieldProtectionArc, provider.get(DamageTypeTags.IS_PROJECTILE).map(Function.identity()), 0, 1));
                reductions.add(new BlocksAttacks.DamageReduction((float) CombatiumLite.shieldProtectionArc, provider.get(DamageTypeTags.IS_EXPLOSION).map(Function.identity()), 0, 1));
            }
            BlocksAttacks blocksAttacks = new BlocksAttacks(0, 1, reductions, BlocksAttacks.ItemDamageFunction.DEFAULT, Optional.of(provider.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)), useShieldSounds ? Optional.of(SoundEvents.SHIELD_BLOCK) : Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_HURT)), useShieldSounds ? Optional.of(SoundEvents.SHIELD_BREAK) : Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_HURT)));
            setter.set(DataComponents.BLOCKS_ATTACKS, blocksAttacks);
        };
    }
    public static BiConsumer<DataComponentTypeSetter, HolderLookup.Provider> useDuration(float useSeconds) {
        return (setter, provider) -> {
            Consumable consumable = setter.get(DataComponents.CONSUMABLE);
            if (consumable != null) {
                Consumable newConsumable = new Consumable(useSeconds, consumable.animation(), consumable.sound(), consumable.hasConsumeParticles(), consumable.onConsumeEffects());
                setter.set(DataComponents.CONSUMABLE, newConsumable);
            }
        };
    }

    public static BiConsumer<DataComponentTypeSetter, HolderLookup.Provider> weapon(float damage, float speed, float reach) {
        return weapon(damage, speed, reach, 1.0F);
    }

    public static BiConsumer<DataComponentTypeSetter, HolderLookup.Provider> weapon(float damage, float speed, float reach, float chargedReach) {
        return (setter, provider) -> {
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_CTS_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            if (reach != 0) builder.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ATTACK_REACH_ID, reach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            setter.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
            List<Component> lore = new ArrayList<>();
            Style commonStyle = CommonComponents.space().getStyle().withItalic(false).withColor(ChatFormatting.GRAY);
            lore.add(CommonComponents.space());
            lore.add(Component.literal("When in Main Hand:").setStyle(commonStyle));
            lore.add(CommonComponents.space().append(Component.literal(format(damage + 2) + " Attack Damage").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));
            lore.add(CommonComponents.space().append(Component.literal(format(speed + 2.5) + " Attack Speed").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));
            if (reach != 0) lore.add(CommonComponents.space().setStyle(Style.EMPTY).append(Component.literal(format(reach + 2.5) + " Attack Reach").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));
            if (chargedReach != 1) lore.add(CommonComponents.space().setStyle(Style.EMPTY).append(Component.literal(format(chargedReach) + " Charged Attack Reach").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));

            setter.set(DataComponents.LORE, new ItemLore(lore));
            setter.set(DataComponents.TOOLTIP_DISPLAY, setter.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true));
            if (chargedReach != 1)
                setter.set(DataComponents.CUSTOM_DATA, setter.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .update(custom -> custom.putFloat(CHARGED_REACH_KEY, chargedReach)));
        };
    }
    public static BiConsumer<DataComponentTypeSetter, HolderLookup.Provider> size(int stackSize) {
        return (setter, provider) -> setter.set(DataComponents.MAX_STACK_SIZE, stackSize);
    }
    public static BiConsumer<DataComponentTypeSetter, HolderLookup.Provider> cooldown(float seconds) {
        return (setter, provider) -> setter.set(DataComponents.USE_COOLDOWN, new UseCooldown(seconds));
    }
    public static BiConsumer<DataComponentTypeSetter, HolderLookup.Provider> weaponComponent(int itemDamagePerAttack, float disableTime) {
        return (setter, provider) -> setter.set(DataComponents.WEAPON, new Weapon(itemDamagePerAttack, disableTime));
    }
    private static String format(double value) {
        DecimalFormat df = new DecimalFormat("0.##");
        return df.format(value);
    }
    public record ItemPatchesEntry(Identifier id, ItemPatches itemPatches) implements Comparable<ItemPatchesEntry> {
        @Override
        public int compareTo(ItemPatchesEntry other) {
            int priority = itemPatches.compareTo(other.itemPatches);
            return priority == 0 ? id.compareTo(other.id) : priority;
        }
    }

    public record DataComponentTypeSetter(PatchedDataComponentMap built, DataComponentMap.Builder builder) {
        public <T> void set(DataComponentType<T> type, T value) {
            set(new TypedDataComponent<>(type, value));
        }

        public void set(TypedDataComponent<?> dataComponent) {
            built.set(dataComponent);
            ItemPatcher.set(builder, dataComponent);
        }

        public <T> @Nullable T get(DataComponentType<T> type) {
            return built.get(type);
        }

        public <T> T getOrDefault(DataComponentType<T> type, T defaultValue) {
            return built.getOrDefault(type, defaultValue);
        }
    }
}
