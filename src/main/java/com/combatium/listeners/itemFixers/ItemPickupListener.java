package com.combatium.listeners.itemFixers;

import com.combatium.util.calculations.FixItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.combatium.CombatiumLite.fasterLiquidDrinking;
import static com.combatium.CombatiumLite.updateLore;

public class ItemPickupListener implements Listener {
    Boolean stackablePotions;
    Boolean visualAttackSpeed;
    Boolean optimizations;

    public ItemPickupListener(Boolean stackablePotions, Boolean visualAttackSpeed, Boolean optimizations) {
        this.stackablePotions = stackablePotions;
        this.visualAttackSpeed = visualAttackSpeed;
        this.optimizations = optimizations;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() == Material.SNOWBALL) {
            event.getItem().getItemStack().setItemMeta(FixItem.fixSnowball(event.getItem().getItemStack()));
        } else if (stackablePotions && event.getItem().getItemStack().getType() == Material.POTION) {
            ItemMeta meta = FixItem.fixPotions(event.getItem().getItemStack());
            if (meta != null) {
                event.getItem().getItemStack().setItemMeta(meta);
            }
        } else if(visualAttackSpeed) {
            ItemMeta meta = FixItem.fixAttackSpeed(event.getItem().getItemStack(), (event.getEntity() instanceof Player player) ? player : null);
            if (meta != null) {
                event.getItem().getItemStack().setItemMeta(meta);
            }
        }

        if (optimizations) {
            ItemMeta meta = FixItem.fixReach(event.getItem().getItemStack());
            if (meta != null) {
                event.getItem().getItemStack().setItemMeta(meta);
            }
        }

        if (updateLore) {
            ItemMeta meta = FixItem.updateLore(event.getItem().getItemStack());
            if (meta != null) {
                event.getItem().getItemStack().setItemMeta(meta);
            }
        }

        if (fasterLiquidDrinking) {
            ItemStack item = FixItem.fixLiquidConsumeTime(event.getItem().getItemStack());
            if (item != null) {
                event.getItem().setItemStack(item);
            }
        }
    }
}
