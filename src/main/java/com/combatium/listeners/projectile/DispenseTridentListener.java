package com.combatium.listeners.projectile;

import io.papermc.paper.event.block.BlockPreDispenseEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class DispenseTridentListener implements Listener {
    @EventHandler
    public void onDispenseTrident(BlockPreDispenseEvent event) {
        //Gets the block instance of the dispenser
        if (!(event.getBlock().getState() instanceof Dispenser dispenser)) return;
        ItemStack item = ((InventoryHolder) dispenser).getInventory().getItem(event.getSlot());

        if (!(item != null && item.getType() == Material.TRIDENT)) return;

        //Gets the direction the dispenser is facing
        Directional directional = (Directional) dispenser.getBlockData();
        Vector direction = directional.getFacing().getDirection();
        Location location = event.getBlock().getLocation();
        location = location.add(0.5, 0.5, 0.5);
        location = location.add(direction.normalize().multiply(0.51));

        //Spawn trident and set data
        Trident trident = (Trident) event.getBlock().getWorld().spawnEntity(location, EntityType.TRIDENT);
        trident.setShooter(dispenser.getBlockProjectileSource());
        trident.setItemStack(item);
        trident.setPickupStatus(Trident.PickupStatus.ALLOWED);
        trident.setVelocity(directional.getFacing().getDirection().normalize() // Direction
                .add(new Vector(Math.random() * 0.1 - 0.05, Math.random() * 0.1 - 0.05, Math.random() * 0.1 - 0.05)) // Randomness
                .multiply(1.3)); // Speed
        ((InventoryHolder) dispenser).getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));

        //Spawn Particles
        location.getWorld().playSound(location, "minecraft:block.dispenser.dispense", 1.0f, 1.0f);
        for (int i = 0; i < 10; i++)
            location.getWorld().spawnParticle(Particle.SMOKE, location, 0, direction.getX(), direction.getY(), direction.getZ(), Math.random() * 0.4);
        event.setCancelled(true);
    }
}
