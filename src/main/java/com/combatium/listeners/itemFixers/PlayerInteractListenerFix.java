package com.combatium.listeners.itemFixers;

import com.combatium.CombatiumLite;
import com.combatium.util.calculations.FixItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListenerFix implements Listener {
    @EventHandler
    public void onEntityInteract(PlayerInteractEvent event) {
        ItemStack weapon = event.getPlayer().getInventory().getItemInMainHand();
        if (weapon.getType().isAir()) return;

        if (!CombatiumLite.swordBlocking) return;

        ItemStack item = FixItem.makeSwordBlock(weapon);
        if (item != null) {
            event.getPlayer().getInventory().setItemInMainHand(item);
        }
    }
}
