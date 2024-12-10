package com.combatium.listeners.itemFixers;

import com.combatium.util.calculations.FixItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.combatium.CombatiumLite.fasterLiquidDrinking;
import static com.combatium.CombatiumLite.updateLore;

public class EntityAttackListener implements Listener {
    Boolean visualAttackSpeed;
    Boolean optimizations;

    public EntityAttackListener(Boolean visualAttackSpeed, Boolean optimizations) {
        this.visualAttackSpeed = visualAttackSpeed;
        this.optimizations = optimizations;
    }

    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {return;}
        if (event.getDamager() instanceof Player player) {
            ItemStack weapon = player.getInventory().getItemInMainHand();
            if (visualAttackSpeed) {
                ItemMeta meta = FixItem.fixAttackSpeed(weapon);
                if (meta != null) {
                    player.getInventory().getItemInMainHand().setItemMeta(meta);
                }
            }

            if (optimizations) {
                ItemMeta meta = FixItem.fixReach(weapon);
                if (meta != null) {
                    player.getInventory().getItemInMainHand().setItemMeta(meta);
                }
            }

            if (updateLore) {
                ItemMeta meta = FixItem.updateLore(weapon);
                if (meta != null) {
                    weapon.setItemMeta(meta);
                }
            }

            if (fasterLiquidDrinking) {
                ItemStack item = FixItem.fixLiquidConsumeTime(weapon);
                if (item != null) {
                    player.getInventory().setItemInMainHand(item);
                }
            }
        }
    }
}
