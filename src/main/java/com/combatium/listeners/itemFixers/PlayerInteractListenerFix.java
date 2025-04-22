package com.combatium.listeners.itemFixers;

import com.combatium.util.calculations.FixItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static com.combatium.CombatiumLite.*;

public class PlayerInteractListenerFix implements Listener {
    @EventHandler
    public void onEntityInteract(PlayerInteractEvent event) {
        ItemStack weapon = event.getPlayer().getInventory().getItemInMainHand();
        if (weapon.getType().isAir()) return;

        if (swordBlocking) {
            ItemStack item = FixItem.makeSwordBlock(weapon);
            if (item != null) {
                event.getPlayer().getInventory().setItemInMainHand(item);
            }
        }
    }
}
