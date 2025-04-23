package com.combatium.util.calculations;

import com.combatium.CombatiumLite;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Sound.ITEM_SHIELD_BREAK;

public class DisableShieldUtil {
    public static void disableShield(Player hitPlayer, ItemStack weapon) {
        int cooldown = 32;
        int cleavingLevel = getCleavingLevel(weapon);
        if (cleavingLevel > 0) {
            cooldown += 10 * cleavingLevel;
        }
        Material activeItem = hitPlayer.getActiveItem().getType();
        if (CombatiumLite.swordBlocking) hitPlayer.setCooldown(activeItem, cooldown);
        else hitPlayer.setCooldown(Material.SHIELD, cooldown);
        hitPlayer.clearActiveItem();
        hitPlayer.getLocation().getWorld().playSound(hitPlayer.getLocation(), ITEM_SHIELD_BREAK, 1, 1);
    }

    public static int getCleavingLevel(ItemStack weapon) {
        for (Enchantment enchantment : weapon.getEnchantments().keySet()) {
            if (enchantment.getKey().equals(NamespacedKey.minecraft("cleaving"))) {
                return weapon.getEnchantmentLevel(enchantment);
            }
        }
        return 0;
    }
}
