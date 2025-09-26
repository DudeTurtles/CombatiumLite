package com.dudeturtles.combatium;

import com.dudeturtles.combatium.util.ItemPatcher;
import com.dudeturtles.combatium.util.MemoizingPredicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.HashCode;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.DynamicOps;
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
import net.minecraft.core.component.*;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.HashOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

public class CombatiumLiteBootstrap implements PluginBootstrap {
    public static MemoizingPredicate<ItemStack> shouldFix = new MemoizingPredicate<>(checkStack -> {
        if (ItemPatcher.DEFAULT_PATCHES.stream().noneMatch(itemPatches -> itemPatches.matchItem(checkStack.getItem()))) return false;
        Field prototype;
        try {
            prototype = PatchedDataComponentMap.class.getDeclaredField("prototype");
            prototype.setAccessible(true);
            if (checkStack.getComponents() instanceof PatchedDataComponentMap current && !(prototype.get(current) instanceof PatchedDataComponentMap)) return true;
        } catch (NoSuchFieldException | IllegalAccessException ignored) {

        }
        return false;
    });
    public static TypedKey<Enchantment> CLEAVING;
    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            Field optionalStreamCodec = ItemStack.class.getDeclaredField("OPTIONAL_STREAM_CODEC");
            Object staticBase = unsafe.staticFieldBase(optionalStreamCodec);
            long staticOffset = unsafe.staticFieldOffset(optionalStreamCodec);
            Field untrustedStreamCodec = ItemStack.class.getDeclaredField("OPTIONAL_UNTRUSTED_STREAM_CODEC");
            Field streamCodec = ItemStack.class.getDeclaredField("STREAM_CODEC");
            Field listStreamCodec = ItemStack.class.getDeclaredField("OPTIONAL_LIST_STREAM_CODEC");
            StreamCodec<RegistryFriendlyByteBuf, ItemStack> original = (StreamCodec<RegistryFriendlyByteBuf, ItemStack>) optionalStreamCodec.get(null); // or use unsafe.getObject(...)
            CombatiumLite.getPluginLogger().log(Level.INFO, "Original stream codec: " + original);
            StreamCodec<RegistryFriendlyByteBuf, ItemStack> patched = wrapStreamCodec(original);

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

            staticBase = unsafe.staticFieldBase(untrustedStreamCodec);
            staticOffset = unsafe.staticFieldOffset(untrustedStreamCodec);
            unsafe.putObject(staticBase, staticOffset, wrapStreamCodec((StreamCodec<RegistryFriendlyByteBuf, ItemStack>) untrustedStreamCodec.get(null)));

            CombatiumLite.getPluginLogger().log(Level.INFO, "Patched stream codec: " + optionalStreamCodec.get(null));

            Field hashedStackStreamCodec = HashedStack.class.getDeclaredField("STREAM_CODEC");
            CombatiumLite.getPluginLogger().log(Level.INFO, "Original stream codec: " + hashedStackStreamCodec.get(null));

            staticBase = unsafe.staticFieldBase(hashedStackStreamCodec);
            staticOffset = unsafe.staticFieldOffset(hashedStackStreamCodec);
            unsafe.putObject(staticBase, staticOffset, ByteBufCodecs.optional(wrapHashedStackStreamCodec((StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem>) HashedStack.ActualItem.class.getDeclaredField("STREAM_CODEC").get(null))).map((optional) -> DataFixUtils.orElse(optional, HashedStack.EMPTY), (hashedStack) -> {
                Optional<HashedStack.ActualItem> optionalActualItem;
                if (hashedStack instanceof HashedStack.ActualItem actualItem) {
                    optionalActualItem = Optional.of(actualItem);
                } else {
                    optionalActualItem = Optional.empty();
                }

                return optionalActualItem;
            }));
            CombatiumLite.getPluginLogger().log(Level.INFO, "Patched stream codec: " + hashedStackStreamCodec.get(null));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ItemStack> wrapStreamCodec(StreamCodec<RegistryFriendlyByteBuf, ItemStack> original) {
        return new StreamCodec<>() {
            @Override
            public ItemStack decode(RegistryFriendlyByteBuf buffer) {
                ItemStack stack = original.decode(buffer);
                DataComponentPatch patch = stack.getComponentsPatch();
                if (stack.getComponents() instanceof PatchedDataComponentMap patchedDataComponentMap) {
                    patchedDataComponentMap.clearPatch();
                    patchedDataComponentMap.applyPatch(patch);
                }
                return stack;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ItemStack stack) {
                if (shouldFix.apply(stack)) {
                    DataComponentPatch patch = stack.getComponentsPatch();
                    try {
                        Field components = ItemStack.class.getDeclaredField("components");
                        components.setAccessible(true);
                        PatchedDataComponentMap patchedDataComponentMap = PatchedDataComponentMap.fromPatch(stack.getPrototype(), patch);
                        components.set(stack, patchedDataComponentMap);
                    } catch (NoSuchFieldException | IllegalAccessException ignored) {

                    }
                }
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
                original.encode(buffer, newStack);
            }
        };
    }
    public static StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> wrapHashedStackStreamCodec(StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> original) {
        return new WrapHashedStreamCodec(original);
    }

    @Override
    public void bootstrap(BootstrapContext context) {
        CLEAVING = EnchantmentKeys.create(Key.key("cleaving"));

        var lifecycleManager = context.getLifecycleManager();
        lifecycleManager.registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(event -> event.registry().register(
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
                        CombatiumLite.getPluginLogger().log(Level.SEVERE, "Failed to set effects! Exception: " + e, e);
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

    private static class WrapHashedStreamCodec implements StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> {
        private final StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> original;
        private DynamicOps<HashCode> registryHashOps;

        private LoadingCache<TypedDataComponent<?>, Integer> cache = CacheBuilder.newBuilder().maximumSize(256L).build(new CacheLoader<>() {
            public Integer load(TypedDataComponent<?> typedDataComponent) {
                return typedDataComponent.encodeValue(WrapHashedStreamCodec.this.registryHashOps).getOrThrow((string) -> {
                    String var10002 = String.valueOf(typedDataComponent);
                    return new IllegalArgumentException("Failed to hash " + var10002 + ": " + string);
                }).asInt();
            }
        });

        public WrapHashedStreamCodec(StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> original) {
            this.original = original;
        }

        @Override
        public HashedStack.ActualItem decode(RegistryFriendlyByteBuf buffer) {
            this.registryHashOps = buffer.registryAccess().createSerializationContext(HashOps.CRC32C_INSTANCE);
            HashedStack.ActualItem stack = original.decode(buffer);
            HashedPatchMap components = stack.components();
            DataComponentMap prototype = stack.item().value().components();
            if (prototype instanceof PatchedDataComponentMap prototypeDataComponentMap) {
                DataComponentPatch.SplitResult splitResult = prototypeDataComponentMap.asPatch().split();
                Map<DataComponentType<?>, Integer> added = new IdentityHashMap<>(splitResult.added().size());
                added.putAll(components.addedComponents());
                splitResult.added().forEach((typedDataComponent) -> {
                    if (added.containsKey(typedDataComponent.type()) && added.get(typedDataComponent.type()).equals(cache.getUnchecked(typedDataComponent))) added.remove(typedDataComponent.type());
                });
                Set<DataComponentType<?>> removed = new HashSet<>(components.removedComponents());
                removed.removeAll(splitResult.removed());
                stack = new HashedStack.ActualItem(stack.item(), stack.count(), new HashedPatchMap(added, removed));
            }
            return stack;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, HashedStack.ActualItem stack) {
            original.encode(buffer, stack);
        }
    }
}
