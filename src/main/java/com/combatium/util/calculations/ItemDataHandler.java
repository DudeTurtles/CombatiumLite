package com.combatium.util.calculations;

import com.combatium.CombatiumLite;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemDataHandler {
    public static double checkItemDataDouble(NamespacedKey key, ItemStack item, double defaultValue) {
        if (item == null || !item.hasItemMeta()) {
            return defaultValue;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(key, PersistentDataType.DOUBLE)) {
            try {
                return container.getOrDefault(key, PersistentDataType.DOUBLE, defaultValue);
            } catch (Exception e) {
                CombatiumLite.logger.warning("Error reading item data: " + e.getMessage());
                return defaultValue;
            }
        } else return defaultValue;
    }

    public static boolean checkItemDataBoolean(NamespacedKey key, ItemStack item, boolean defaultValue) {
        if (item == null || !item.hasItemMeta()) {
            return defaultValue;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(key, PersistentDataType.BOOLEAN)) {
            try {
                return container.getOrDefault(key, PersistentDataType.BOOLEAN, defaultValue);
            } catch (Exception e) {
                CombatiumLite.logger.warning("Error reading item data: " + e.getMessage());
                return defaultValue;
            }
        } else {
            container.set(key, PersistentDataType.BOOLEAN, defaultValue);
            item.setItemMeta(meta);
            return defaultValue;
        }
    }
}
