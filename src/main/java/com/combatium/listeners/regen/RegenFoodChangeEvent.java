package com.combatium.listeners.regen;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class RegenFoodChangeEvent implements Listener {
    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.isCancelled()) return;
        /*
         * The item is used to identify events which are called due to regen exhaustion.
         * Since the event is called by the plugin, it won't properly implement the impact in game,
         * so adding this check ensures the food is properly changed, and the event is cancellable.
         */
        if (event.getItem() != null && event.getItem().getType() == Material.WAXED_WEATHERED_CUT_COPPER_STAIRS)
            event.getEntity().setFoodLevel(event.getFoodLevel());
    }
}
