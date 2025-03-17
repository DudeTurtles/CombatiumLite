package com.combatium.util.calculations;

import com.combatium.CombatiumLite;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

import static org.bukkit.Sound.ITEM_SHIELD_BREAK;

public class DisableShieldUtil {
    public static void disableShield(Player hitPlayer, ItemStack weapon) {
        int cooldown = 32;
        int cleavingLevel = getCleavingLevel(weapon);
        if (cleavingLevel > 0) {
            cooldown += 10 * cleavingLevel;
        }
        Material activeItem = hitPlayer.getActiveItem().getType();
        hitPlayer.clearActiveItem();
        if (CombatiumLite.swordBlocking) hitPlayer.setCooldown(activeItem, cooldown);
        else hitPlayer.setCooldown(Material.SHIELD, cooldown);
        for (Entity entity : hitPlayer.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof Player nearbyPlayer) {
                nearbyPlayer.playSound(nearbyPlayer.getLocation(), ITEM_SHIELD_BREAK, 1, 1);
            }
        }
    }

    public static int getCleavingLevel(ItemStack weapon) {
        for (Enchantment enchantment : weapon.getEnchantments().keySet()) {
            if (enchantment.toString().contains("cleaving")) {
                return weapon.getEnchantmentLevel(enchantment);
            }
        }
        return 0;
    }

    public static boolean isAxe(@Nullable ItemStack weapon) {
        if (weapon == null) return false;
        return weapon.getType() == Material.WOODEN_AXE || weapon.getType() == Material.STONE_AXE ||
                weapon.getType() == Material.GOLDEN_AXE || weapon.getType() == Material.IRON_AXE ||
                weapon.getType() == Material.DIAMOND_AXE || weapon.getType() == Material.NETHERITE_AXE;
    }
}
