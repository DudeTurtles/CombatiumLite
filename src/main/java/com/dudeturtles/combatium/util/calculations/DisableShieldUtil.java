package com.dudeturtles.combatium.util.calculations;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;

public class DisableShieldUtil {
    public static final ResourceKey<@NotNull Enchantment> CLEAVING = ResourceKey.create(Registries.ENCHANTMENT, Identifier.withDefaultNamespace("cleaving"));
    public static int getCleavingLevel(ItemEnchantments enchantments, RegistryAccess registryAccess) {
        return enchantments.getLevel(registryAccess.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(CLEAVING));
    }
}
