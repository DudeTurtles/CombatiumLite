package com.combatium.util;

import com.combatium.enchants.CustomEnchants;
import com.combatium.util.calculations.FixItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class API {
    public static ItemStack getFixedItem(Material material, boolean swordBlocking) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = FixItem.fixAttackSpeedAndDamage(item);
        if (itemMeta != null) {
            item.setItemMeta(itemMeta);
        }
        itemMeta = FixItem.fixReach(item);
        if (itemMeta != null) {
            item.setItemMeta(itemMeta);
        }
        itemMeta = FixItem.updateLore(item);
        if (itemMeta != null) {
            item.setItemMeta(itemMeta);
        }
        if (swordBlocking) {
            if (FixItem.isSword(item.getType())) {
                item = FixItem.makeSwordBlock(item);
                if (item != null) {
                    item.setItemMeta(itemMeta);
                    return item;
                }
            }
        }
        return item;
    }

    public static Enchantment getCleaving() {
        return CustomEnchants.cleaving;
    }
}
