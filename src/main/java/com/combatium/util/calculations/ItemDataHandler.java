package com.combatium.util.calculations;

import com.combatium.CombatiumLite;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemDataHandler {
    public static double checkItemDataDouble(NamespacedKey key, ItemStack item, double defaultValue) {
        if (item == null) {
            return -1;
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(key, PersistentDataType.DOUBLE)) {
            try {
                return container.get(key, PersistentDataType.DOUBLE);
            } catch (Exception e) {
                CombatiumLite.logger.warning("Error reading item data: " + e.getMessage());
                return defaultValue;
            }
        } else {
            container.set(key, PersistentDataType.DOUBLE, defaultValue);
            return defaultValue;
        }
    }
}
