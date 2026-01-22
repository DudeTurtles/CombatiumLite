package com.dudeturtles.combatium.util.calculations;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DisableShieldUtil {
    public static int getCleavingLevel(ItemStack weapon) {
        Enchantment cleaving = getCleaving();
        ItemMeta itemMeta = weapon.getItemMeta();
        if (itemMeta.hasEnchant(cleaving)) return itemMeta.getEnchantLevel(cleaving);
        return 0;
    }

    public static Enchantment getCleaving() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(NamespacedKey.minecraft("cleaving"));
    }
}
