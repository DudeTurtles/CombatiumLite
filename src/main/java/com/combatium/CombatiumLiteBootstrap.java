package com.combatium;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;

public class CombatiumLiteBootstrap implements PluginBootstrap {
    public static TypedKey<Enchantment> CLEAVING;
    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            Field streamCodec = ItemStack.class.getDeclaredField("OPTIONAL_STREAM_CODEC");
            Object staticBase = unsafe.staticFieldBase(streamCodec);
            long staticOffset = unsafe.staticFieldOffset(streamCodec);
            StreamCodec<RegistryFriendlyByteBuf, ItemStack> original = (StreamCodec<RegistryFriendlyByteBuf, ItemStack>) streamCodec.get(null); // or use unsafe.getObject(...)
            CombatiumLite.logger.log(Level.INFO, "Original stream codec: " + original);
            StreamCodec<RegistryFriendlyByteBuf, ItemStack> patched = new StreamCodec<>() {
                @Override
                public ItemStack decode(RegistryFriendlyByteBuf buffer) {
                    return original.decode(buffer);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ItemStack stack) {
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
                    original.encode(buffer, stack);
                }
            };

            unsafe.putObject(staticBase, staticOffset, patched);
            CombatiumLite.logger.log(Level.INFO, "Patched stream codec: " + streamCodec.get(null));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void bootstrap(BootstrapContext context) {
        CLEAVING = EnchantmentKeys.create(Key.key("cleaving"));

        var lifecycleManager = context.getLifecycleManager();
        lifecycleManager.registerEventHandler(RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> {
            event.registry().register(
                    CLEAVING,
                    b -> b.description(Component.text("Cleaving"))
                            .primaryItems(event.getOrCreateTag(ItemTypeTagKeys.AXES))
                            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.AXES))
                            .anvilCost(4)
                            .maxLevel(3)
                            .weight(10)
                            .exclusiveWith(RegistrySet.keySet(
                                    RegistryKey.ENCHANTMENT,
                                    EnchantmentKeys.SHARPNESS,
                                    EnchantmentKeys.BANE_OF_ARTHROPODS,
                                    EnchantmentKeys.SMITE)
                            )
                            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 11))
                            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(12, 11))
                            .activeSlots(EquipmentSlotGroup.HAND)
            );

        }));

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
