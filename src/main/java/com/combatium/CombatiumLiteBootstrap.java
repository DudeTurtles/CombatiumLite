package com.combatium;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.data.PaperEnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

import static com.combatium.util.ItemPatcher.addAttributeTooltips;

public class CombatiumLiteBootstrap implements PluginBootstrap {
    public static TypedKey<Enchantment> CLEAVING;
    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            Field optionalStreamCodec = ItemStack.class.getDeclaredField("OPTIONAL_STREAM_CODEC");
            Object staticBase = unsafe.staticFieldBase(optionalStreamCodec);
            long staticOffset = unsafe.staticFieldOffset(optionalStreamCodec);
            Field streamCodec = ItemStack.class.getDeclaredField("STREAM_CODEC");
            Field listStreamCodec = ItemStack.class.getDeclaredField("OPTIONAL_LIST_STREAM_CODEC");
            StreamCodec<RegistryFriendlyByteBuf, ItemStack> original = (StreamCodec<RegistryFriendlyByteBuf, ItemStack>) optionalStreamCodec.get(null); // or use unsafe.getObject(...)
            CombatiumLite.logger.log(Level.INFO, "Original stream codec: " + original);
            StreamCodec<RegistryFriendlyByteBuf, ItemStack> patched = new StreamCodec<>() {
                @Override
                public ItemStack decode(RegistryFriendlyByteBuf buffer) {
                    return original.decode(buffer);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ItemStack stack) {
                    ItemStack newStack = stack.copy();
                    if (!stack.isEmpty()) {
                        DataComponentMap prototype = stack.getPrototype();
                        if (prototype instanceof PatchedDataComponentMap prototypeDataComponentMap) {
                            if (newStack.getComponents() instanceof PatchedDataComponentMap patchedDataComponentMap) {
                                patchedDataComponentMap.restorePatch(prototypeDataComponentMap.asPatch());
                                patchedDataComponentMap.applyPatch(stack.getComponentsPatch());
                            }
                        }
                    }
                    ServerPlayer player = Bukkit.getOnlinePlayers().stream().findFirst().map(bukkit -> ((CraftPlayer)bukkit).getHandle()).orElse(null);
                    ItemLore originalLore = newStack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
                    List<net.minecraft.network.chat.Component> lines = new ArrayList<>();
                    List<net.minecraft.network.chat.Component> styledLines = originalLore.styledLines();
                    addAttributeTooltips(lines::add, newStack, player);
                    lines.addAll(originalLore.lines());
                    ItemLore newLore = new ItemLore(lines, styledLines);
                    if (!newLore.equals(originalLore)) {
                        newStack.set(DataComponents.LORE, newLore);
                        newStack.set(DataComponents.ATTRIBUTE_MODIFIERS, newStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).withTooltip(false));
                    }
                    original.encode(buffer, newStack);
                }
            };

            unsafe.putObject(staticBase, staticOffset, patched);

            staticBase = unsafe.staticFieldBase(streamCodec);
            staticOffset = unsafe.staticFieldOffset(streamCodec);
            unsafe.putObject(staticBase, staticOffset, new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
                @Override
                public ItemStack decode(RegistryFriendlyByteBuf buffer) {
                    ItemStack itemStack = patched.decode(buffer);
                    if (itemStack.isEmpty()) {
                        throw new DecoderException("Empty ItemStack not allowed");
                    } else {
                        return itemStack;
                    }
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ItemStack value) {
                    if (value.isEmpty()) {
                        throw new EncoderException("Empty ItemStack not allowed");
                    } else {
                        patched.encode(buffer, value);
                    }
                }
            });

            staticBase = unsafe.staticFieldBase(listStreamCodec);
            staticOffset = unsafe.staticFieldOffset(listStreamCodec);
            unsafe.putObject(staticBase, staticOffset, patched.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)));

            CombatiumLite.logger.log(Level.INFO, "Patched stream codec: " + optionalStreamCodec.get(null));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void bootstrap(BootstrapContext context) {
        CLEAVING = EnchantmentKeys.create(Key.key("cleaving"));

        var lifecycleManager = context.getLifecycleManager();
        lifecycleManager.registerEventHandler(RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> event.registry().register(
                CLEAVING,
                b -> {
                    b.description(Component.translatable("enchantment.minecraft.cleaving", "Cleaving"))
                            .primaryItems(event.getOrCreateTag(ItemTypeTagKeys.AXES))
                            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.AXES))
                            .anvilCost(4)
                            .maxLevel(3)
                            .weight(10)
                            .exclusiveWith(RegistrySet.keySet(
                                    RegistryKey.ENCHANTMENT,
                                    EnchantmentKeys.SHARPNESS,
                                    EnchantmentKeys.BANE_OF_ARTHROPODS,
                                    EnchantmentKeys.SMITE,
                                    EnchantmentKeys.IMPALING)
                            )
                            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 11))
                            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(12, 11))
                            .activeSlots(EquipmentSlotGroup.HAND);
                    try {
                        Field effects = PaperEnchantmentRegistryEntry.class.getDeclaredField("effects");
                        effects.setAccessible(true);
                        PatchedDataComponentMap patchedEffects = new PatchedDataComponentMap((DataComponentMap) effects.get(b));
                        patchedEffects.set(EnchantmentEffectComponents.DAMAGE, Collections.singletonList(new ConditionalEffect<>(new AddValue(LevelBasedValue.perLevel(2, 1)), Optional.empty())));
                        effects.set(b, patchedEffects);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        CombatiumLite.logger.log(Level.SEVERE, "Failed to set effects! Exception: " + e, e);
                    }
                }
        )));

        Set<TypedKey<Enchantment>> singleton = Collections.singleton(CLEAVING);
        lifecycleManager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT), event -> {
            var registrar = event.registrar();
            registrar.addToTag(EnchantmentTagKeys.TREASURE, singleton);
            registrar.addToTag(EnchantmentTagKeys.TRADEABLE, singleton);
            registrar.addToTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE, singleton);
        });

        for (TypedKey<Enchantment> enchantmentTypedKey : Arrays.asList(EnchantmentKeys.KNOCKBACK, EnchantmentKeys.SWEEPING_EDGE, EnchantmentKeys.LOOTING)) {
            context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd()
                    .newHandler(event -> event.builder().supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_SHARP_WEAPON)))
                    .filter(enchantmentTypedKey)
            );
        }

        /*
         * The reason why fire aspect isn't in the loop is that you can enchant maces with fire aspect too
         * but the tag enchantable/sharp_weapon doesn't include maces
         */
        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd()
                .newHandler(event -> event.builder().supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_WEAPON)))
                .filter(EnchantmentKeys.FIRE_ASPECT)
        );
    }
}
