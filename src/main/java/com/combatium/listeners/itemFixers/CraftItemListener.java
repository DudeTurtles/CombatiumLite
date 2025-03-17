package com.combatium.listeners.itemFixers;

import com.combatium.CombatiumLite;
import com.combatium.util.calculations.FixItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.combatium.CombatiumLite.fasterLiquidDrinking;

public class CraftItemListener implements Listener {
    Boolean visualChanges;
    Boolean optimizations;

    public CraftItemListener(Boolean visualChanges, Boolean optimizations) {
        this.visualChanges = visualChanges;
        this.optimizations = optimizations;
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }

        if(visualChanges) {
            ItemMeta meta = FixItem.fixAttackSpeedAndDamage(event.getCurrentItem());
            if (meta != null) {
                event.getCurrentItem().setItemMeta(meta);
            }
        }

        if (optimizations) {
            ItemMeta meta = FixItem.fixReach(event.getCurrentItem());
            if (meta != null) {
                event.getCurrentItem().setItemMeta(meta);
            }
        }

        if (CombatiumLite.updateLore) {
            ItemMeta meta = FixItem.updateLore(event.getCurrentItem());
            if (meta != null) {
                event.getCurrentItem().setItemMeta(meta);
            }
        }

        if (CombatiumLite.swordBlocking) {
            ItemStack item = FixItem.makeSwordBlock(event.getCurrentItem());
            if (item != null) {
                event.setCurrentItem(item);
            }
        }

        if (fasterLiquidDrinking) {
            ItemStack item = FixItem.fixLiquidConsumeTime(event.getCurrentItem());
            if (item != null) {
                event.setCurrentItem(item);
            }
        }
    }
}
