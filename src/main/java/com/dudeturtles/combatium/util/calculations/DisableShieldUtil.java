package com.dudeturtles.combatium.util.calculations;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class DisableShieldUtil {
    public static int getCleavingLevel(ItemStack weapon) {
        for (Enchantment enchantment : weapon.getEnchantments().keySet()) {
            if (enchantment.getKey().equals(NamespacedKey.minecraft("cleaving"))) {
                return weapon.getEnchantmentLevel(enchantment);
            }
        }
        return 0;
    }
}
