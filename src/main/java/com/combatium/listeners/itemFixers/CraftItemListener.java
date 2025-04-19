package com.combatium.listeners.itemFixers;

import com.combatium.CombatiumLite;
import com.combatium.util.calculations.FixItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CraftItemListener implements Listener {
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }

        ItemMeta meta = FixItem.updateLore(event.getCurrentItem());
        if (meta != null) {
            event.getCurrentItem().setItemMeta(meta);
        }

        if (CombatiumLite.swordBlocking) {
            ItemStack item = FixItem.makeSwordBlock(event.getCurrentItem());
            if (item != null) {
                event.setCurrentItem(item);
            }
        }
    }
}
