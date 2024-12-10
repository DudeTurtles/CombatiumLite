package com.combatium.listeners.itemFixers;

import com.combatium.util.calculations.FixItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.combatium.CombatiumLite.*;

public class PlayerInteractListenerFix implements Listener {
    Boolean visualAttackSpeed;
    Boolean optimizations;

    public PlayerInteractListenerFix(Boolean visualAttackSpeed, Boolean optimizations) {
        this.visualAttackSpeed = visualAttackSpeed;
        this.optimizations = optimizations;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEvent event) {
        ItemStack weapon = event.getPlayer().getInventory().getItemInMainHand();
        if (weapon.getType().isAir()) return;

        if (visualAttackSpeed) {
            ItemMeta meta = FixItem.fixAttackSpeed(weapon);
            if (meta != null) {
                event.getPlayer().getInventory().getItemInMainHand().setItemMeta(meta);
            }
        }

        if (optimizations) {
            ItemMeta meta = FixItem.fixReach(weapon);
            if (meta != null) {
                event.getPlayer().getInventory().getItemInMainHand().setItemMeta(meta);
            }
        }

        if (updateLore) {
            ItemMeta meta = FixItem.updateLore(weapon);
            if (meta != null) {
                weapon.setItemMeta(meta);
            }
        }

        if (swordBlocking) {
            ItemStack item = FixItem.makeSwordBlock(weapon);
            if (item != null) {
                event.getPlayer().getInventory().setItemInMainHand(item);
            }
        }

        if (fasterLiquidDrinking) {
            ItemStack item = FixItem.fixLiquidConsumeTime(weapon);
            if (item != null) {
                event.getPlayer().getInventory().setItemInMainHand(item);
            }
        }
    }
}
