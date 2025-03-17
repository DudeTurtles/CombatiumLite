package com.combatium.listeners.util;

import com.combatium.CombatiumLite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class ConsumeListener implements Listener {
    @EventHandler
    public void onSaturationChange(PlayerItemConsumeEvent event) {
        if (event.isCancelled() || !CombatiumLite.saturationChanges) return;

        ItemStack item = ItemStack.fromBukkitCopy(event.getItem());
        FoodProperties itemConsume = item.getComponents().get(DataComponents.FOOD);
        if (itemConsume == null) return;
        float saturation = itemConsume.saturation();
        float currentSaturation = event.getPlayer().getSaturation();
        event.getPlayer().setSaturation(Math.max(0, currentSaturation - saturation));
    }
}
