package com.combatium.listeners.itemFixers;

import com.combatium.util.calculations.FixItem;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.combatium.CombatiumLite.fasterLiquidDrinking;
import static com.combatium.CombatiumLite.updateLore;

public class InventoryClickListener implements Listener {
    Boolean stackablePotions;
    Boolean visualAttackSpeed;
    Boolean optimizations;

    public InventoryClickListener(Boolean stackablePotions, Boolean visualAttackSpeed, Boolean optimizations) {
        this.stackablePotions = stackablePotions;
        this.visualAttackSpeed = visualAttackSpeed;
        this.optimizations = optimizations;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }
        if (event.getCurrentItem().getType() == Material.SNOWBALL) {
            event.getCurrentItem().setItemMeta(FixItem.fixSnowball(event.getCurrentItem()));
        } else if (stackablePotions && event.getCurrentItem().getType() == Material.POTION) {
            ItemMeta meta = FixItem.fixPotions(event.getCurrentItem());
            if (meta != null) {
                event.getCurrentItem().setItemMeta(meta);
            }
        } else if(visualAttackSpeed) {
            ItemMeta meta = FixItem.fixAttackSpeed(event.getCurrentItem());
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

        if (updateLore) {
            ItemMeta meta = FixItem.updateLore(event.getCurrentItem());
            if (meta != null) {
                event.getCurrentItem().setItemMeta(meta);
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
