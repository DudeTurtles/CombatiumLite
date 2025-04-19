package com.combatium.listeners.itemFixers;

import com.combatium.util.calculations.FixItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemPickupListener implements Listener {
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        ItemMeta meta = FixItem.updateLore(event.getItem().getItemStack());
        if (meta != null) {
            event.getItem().getItemStack().setItemMeta(meta);
        }
    }
}
