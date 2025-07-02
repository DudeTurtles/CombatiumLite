package com.dudeturtles.combatium.util;

import com.dudeturtles.combatium.CombatiumLite;
import com.google.common.base.Suppliers;
import net.atlas.defaulted.component.ItemPatches;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.*;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.inventory.CraftItemMetas;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public class ItemPatcher {
    public static final String MOD_ID = "combatium";
    public static List<ItemPatches> DEFAULT_PATCHES = new ArrayList<>();
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
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
            Map<ResourceLocation, ItemPatches> intermediary = new HashMap<>();
            // Patch components here
            applyDefaultComponents(useDuration(1), id("drinkables"), intermediary, Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP,
                    Items.SUSPICIOUS_STEW, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.MILK_BUCKET, Items.HONEY_BOTTLE, Items.OMINOUS_BOTTLE);
            applyDefaultComponents(size(16).andThen(useDuration(1)), intermediary, Items.POTION);
            applyDefaultComponents(size(64).andThen(cooldown(0.2F)), intermediary, Items.SNOWBALL);
            applyDefaultComponents(cooldown(0.2F), id("eggs"), intermediary, Items.EGG, Items.BROWN_EGG, Items.BLUE_EGG);

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
            applyDefaultComponents(blocksAttacks(5, 0, true, true), intermediary, Items.SHIELD);

            applyDefaultComponents(weapon(1.0F, 0.5F, 0.5F), intermediary, Items.WOODEN_SWORD);
            applyDefaultComponents(weapon(1.0F, 0.5F, 0.5F), intermediary, Items.GOLDEN_SWORD);
            applyDefaultComponents(weapon(2.0F, 0.5F, 0.5F), intermediary, Items.STONE_SWORD);
            applyDefaultComponents(weapon(3.0F, 0.5F, 0.5F), intermediary, Items.IRON_SWORD);
            applyDefaultComponents(weapon(4.0F, 0.5F, 0.5F), intermediary, Items.DIAMOND_SWORD);
            applyDefaultComponents(weapon(5.0F, 0.5F, 0.5F), intermediary, Items.NETHERITE_SWORD);

            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(3.0F, -0.5F, 0.0F)), intermediary, Items.WOODEN_AXE);
            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(3.0F, -0.5F, 0.0F)), intermediary, Items.GOLDEN_AXE);
            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(3.0F, -0.5F, 0.0F)), intermediary, Items.STONE_AXE);
            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(4.0F, -0.5F, 0.0F)), intermediary, Items.IRON_AXE);
            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(5.0F, -0.5F, 0.0F)), intermediary, Items.DIAMOND_AXE);
            applyDefaultComponents(weaponComponent(1, 1.6F).andThen(weapon(6.0F, -0.5F, 0.0F)), intermediary, Items.NETHERITE_AXE);

            applyDefaultComponents(weapon(0.0F, -0.5F, 1.0F), intermediary, Items.WOODEN_HOE);
            applyDefaultComponents(weapon(0.0F, 1.0F, 1.0F), intermediary, Items.GOLDEN_HOE);
            applyDefaultComponents(weapon(0.0F, 0.0F, 1.0F), intermediary, Items.STONE_HOE);
            applyDefaultComponents(weapon(1.0F, 0.5F, 1.0F), intermediary, Items.IRON_HOE);
            applyDefaultComponents(weapon(1.0F, 1.0F, 1.0F), intermediary, Items.DIAMOND_HOE);
            applyDefaultComponents(weapon(2.0F, 1.0F, 1.0F), intermediary, Items.NETHERITE_HOE);

            applyDefaultComponents(weapon(0.0F, -0.5F, 0.0F), intermediary, Items.WOODEN_SHOVEL);
            applyDefaultComponents(weapon(0.0F, -0.5F, 0.0F), intermediary, Items.GOLDEN_SHOVEL);
            applyDefaultComponents(weapon(0.0F, -0.5F, 0.0F), intermediary, Items.STONE_SHOVEL);
            applyDefaultComponents(weapon(1.0F, -0.5F, 0.0F), intermediary, Items.IRON_SHOVEL);
            applyDefaultComponents(weapon(2.0F, -0.5F, 0.0F), intermediary, Items.DIAMOND_SHOVEL);
            applyDefaultComponents(weapon(3.0F, -0.5F, 0.0F), intermediary, Items.NETHERITE_SHOVEL);

            applyDefaultComponents(weapon(1.0F, 0.0F, 0.0F), intermediary, Items.WOODEN_PICKAXE);
            applyDefaultComponents(weapon(1.0F, 0.0F, 0.0F), intermediary, Items.GOLDEN_PICKAXE);
            applyDefaultComponents(weapon(1.0F, 0.0F, 0.0F), intermediary, Items.STONE_PICKAXE);
            applyDefaultComponents(weapon(2.0F, 0.0F, 0.0F), intermediary, Items.IRON_PICKAXE);
            applyDefaultComponents(weapon(3.0F, 0.0F, 0.0F), intermediary, Items.DIAMOND_PICKAXE);
            applyDefaultComponents(weapon(4.0F, 0.0F, 0.0F), intermediary, Items.NETHERITE_PICKAXE);

            applyDefaultComponents(weapon(5.0F, -0.5F, 1.0F), intermediary, Items.TRIDENT);
            applyDefaultComponents(weapon(4.0F, -1.50F, 0.0F), intermediary, Items.MACE);
            DEFAULT_PATCHES = new ArrayList<>(intermediary.entrySet().stream().map(entry -> new ItemPatchesEntry(entry.getKey(), entry.getValue())).sorted(Comparator.naturalOrder()).map(ItemPatchesEntry::itemPatches).toList());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            CombatiumLite.getPluginLogger().log(Level.SEVERE, "Failed to update default components!" + e);
        }
    }

    public static void applyDefaultComponents(Consumer<PatchedDataComponentMap> patcher, Map<ResourceLocation, ItemPatches> intermediary, Item item) throws NoSuchFieldException, IllegalAccessException {
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
        intermediary.put(id(item.builtInRegistryHolder().key().location().getPath()), new ItemPatches(HolderSet.direct(item.builtInRegistryHolder()), Collections.emptyList(), Collections.emptyList(), newMap.asPatch(), 1000));
    }
    public static <I extends ItemMeta> CraftItemMetas.ItemMetaData<I> generate(CraftItemMetas.ItemMetaData<I> meta) {
        return new CraftItemMetas.ItemMetaData<>(meta.metaClass(), (stack, extra) -> (I) WRAPPER.apply(meta, stack, extra), meta.fromItemMeta());
    }
    public static void applyDefaultComponents(Consumer<PatchedDataComponentMap> patcher, ResourceLocation patchName, Map<ResourceLocation, ItemPatches> intermediary, Item... items) throws NoSuchFieldException, IllegalAccessException {
        Field components = Item.class.getDeclaredField("components");
        components.setAccessible(true);

        Field metaData = CraftItemType.class.getDeclaredField("itemMetaData");
        metaData.setAccessible(true);
        PatchedDataComponentMap generic = new PatchedDataComponentMap(DataComponentMap.builder().build());
        patcher.accept(generic);
        for (Item item : items) {
            PatchedDataComponentMap newMap = new PatchedDataComponentMap(item.components());
            patcher.accept(newMap);
            components.set(item, newMap);

            if (CraftItemType.minecraftToBukkitNew(item) instanceof CraftItemType<?> craftItemType) {
                metaData.set(craftItemType, Suppliers.memoize(() -> generate(CraftItemMetas.getItemMetaData(craftItemType))));
            }
        }
        intermediary.put(patchName, new ItemPatches(HolderSet.direct(Item::builtInRegistryHolder, items), Collections.emptyList(), Collections.emptyList(), generic.asPatch(), 1000));
    }
    public static Consumer<PatchedDataComponentMap> blocksAttacks(float baseProt, float factor, boolean negateProjectiles, boolean useShieldSounds) {
        return patchedDataComponentMap -> {
            List<BlocksAttacks.DamageReduction> reductions = new ArrayList<>();
            reductions.add(new BlocksAttacks.DamageReduction((float) CombatiumLite.shieldProtectionArc, Optional.empty(), baseProt, factor));
            if (negateProjectiles) {
                Registry<DamageType> damageRegistry = CraftRegistry.getMinecraftRegistry(Registries.DAMAGE_TYPE);
                reductions.add(new BlocksAttacks.DamageReduction((float) CombatiumLite.shieldProtectionArc, damageRegistry.get(DamageTypeTags.IS_PROJECTILE).map(Function.identity()), 0, 1));
                reductions.add(new BlocksAttacks.DamageReduction((float) CombatiumLite.shieldProtectionArc, damageRegistry.get(DamageTypeTags.IS_EXPLOSION).map(Function.identity()), 0, 1));
            }
            BlocksAttacks blocksAttacks = new BlocksAttacks(0, 1, reductions, BlocksAttacks.ItemDamageFunction.DEFAULT, Optional.of(DamageTypeTags.BYPASSES_SHIELD), useShieldSounds ? Optional.of(SoundEvents.SHIELD_BLOCK) : Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_HURT)), useShieldSounds ? Optional.of(SoundEvents.SHIELD_BREAK) : Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_HURT)));
            patchedDataComponentMap.set(DataComponents.BLOCKS_ATTACKS, blocksAttacks);
        };
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
            List<Component> lore = new ArrayList<>();
            Style commonStyle = CommonComponents.space().getStyle().withItalic(false).withColor(ChatFormatting.GRAY);
            lore.add(CommonComponents.space());
            lore.add(Component.literal("When in Main Hand:").setStyle(commonStyle));
            lore.add(CommonComponents.space().append(Component.literal(format(damage + 2) + " Attack Damage").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));
            lore.add(CommonComponents.space().append(Component.literal(format(speed + 2.5) + " Attack Speed").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));
            if (reach != 0) lore.add(CommonComponents.space().setStyle(Style.EMPTY).append(Component.literal(format(reach + 2.5) + " Attack Reach").setStyle(commonStyle).withStyle(ChatFormatting.DARK_GREEN)));

            patchedDataComponentMap.set(DataComponents.LORE, new ItemLore(lore));
            patchedDataComponentMap.set(DataComponents.TOOLTIP_DISPLAY, patchedDataComponentMap.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true));
        };
    }
    public static Consumer<PatchedDataComponentMap> size(int stackSize) {
        return patchedDataComponentMap -> patchedDataComponentMap.set(DataComponents.MAX_STACK_SIZE, stackSize);
    }
    public static Consumer<PatchedDataComponentMap> cooldown(float seconds) {
        return patchedDataComponentMap -> patchedDataComponentMap.set(DataComponents.USE_COOLDOWN, new UseCooldown(seconds));
    }
    public static Consumer<PatchedDataComponentMap> weaponComponent(int itemDamagePerAttack, float disableTime) {
        return patchedDataComponentMap -> patchedDataComponentMap.set(DataComponents.WEAPON, new Weapon(itemDamagePerAttack, disableTime));
    }
    private static String format(double value) {
        DecimalFormat df = new DecimalFormat("0.#");
        return df.format(value);
    }
    public record ItemPatchesEntry(ResourceLocation id, ItemPatches itemPatches) implements Comparable<ItemPatchesEntry> {
        @Override
        public int compareTo(ItemPatchesEntry other) {
            int priority = itemPatches.compareTo(other.itemPatches);
            return priority == 0 ? id.compareTo(other.id) : priority;
        }
    }
}
