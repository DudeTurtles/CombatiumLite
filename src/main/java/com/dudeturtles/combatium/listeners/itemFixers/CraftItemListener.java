package com.dudeturtles.combatium.listeners.itemFixers;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.util.calculations.FixItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftItemListener implements Listener {
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }

        if (!CombatiumLite.swordBlocking) return;

        ItemStack item = FixItem.makeSwordBlock(event.getCurrentItem());
        if (item != null) {
            event.setCurrentItem(item);
        }
    }
}
