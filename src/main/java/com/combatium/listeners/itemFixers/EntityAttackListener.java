package com.combatium.listeners.itemFixers;

import com.combatium.util.calculations.FixItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EntityAttackListener implements Listener {
    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {return;}
        if (event.getDamager() instanceof Player player) {
            ItemStack weapon = player.getInventory().getItemInMainHand();

            ItemMeta meta = FixItem.updateLore(weapon);
            if (meta != null) {
                weapon.setItemMeta(meta);
            }
        }
    }
}
