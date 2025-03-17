package com.combatium.listeners.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.entity.CraftVehicle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.ItemStack;

public class VehicleDamageListener implements Listener {
    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getAttacker() instanceof Player player) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.containsEnchantment(Enchantment.SWEEPING_EDGE)) return;
            event.setCancelled(true);
            Entity vehicle = ((CraftVehicle) event.getVehicle()).getHandle();
            DamageSource damageSource = vehicle.damageSources().generic();
            vehicle.hurtServer(vehicle.level().getMinecraftWorld(), damageSource, (float) event.getDamage());
        }
    }
}
