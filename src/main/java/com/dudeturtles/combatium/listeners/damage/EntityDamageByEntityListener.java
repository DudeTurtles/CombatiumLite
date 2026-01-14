package com.dudeturtles.combatium.listeners.damage;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.scheduler.InvulnerabilityScheduler;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.HashMap;

public class EntityDamageByEntityListener implements Listener {
    private final InvulnerabilityScheduler invulnerabilityScheduler;
    private final HashMap<Player, Boolean> wasHitCancelled = new HashMap<>();

    public EntityDamageByEntityListener() {
        this.invulnerabilityScheduler = CombatiumLite.getScheduler().getInvulnerabilityScheduler();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void cancelChecker(EntityDamageByEntityEvent event) {
        if (event.isCancelled() && event.getDamager() instanceof Player player) wasHitCancelled.put(player, true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityDamageByEntity(EntityDamageByEntityEvent event) {
        DamageType damageType = event.getDamageSource().getDamageType();
        if (damageType == DamageType.GENERIC_KILL || damageType == DamageType.INDIRECT_MAGIC || damageType == DamageType.THORNS) {
            return;
        }

        if (event.getDamager() instanceof Trident trident) { // Updated trident damage calculation
            double damage = 8;
            if (trident.getItemStack().containsEnchantment(Enchantment.IMPALING) && (event.getEntity()).isInWaterOrRain()) {
                damage += trident.getItemStack().getEnchantmentLevel(Enchantment.IMPALING) * 2.5;
            }
            event.setDamage(damage);
        }
        if (event.getEntity() instanceof LivingEntity victim && !(event.getDamager() instanceof Player)) { // Ensure replacement i-frames work for other cases
            double invulDamage = invulnerabilityScheduler.isInvulnerable(victim);

            if (invulDamage >= event.getDamage() && invulDamage != 0) {
                event.setCancelled(true);
                event.setDamage(0);
                return;
            }

            if (event.isCancelled()) return;

            double originalDamage = event.getDamage();
            event.setDamage(event.getDamage() - invulDamage);

            DamageSource damageSource = ((CraftDamageSource)event.getDamageSource()).getHandle();

            int ticks = 10;

            if (damageSource.is(DamageTypeTags.IS_PROJECTILE))
                ticks = 0;

            invulnerabilityScheduler.setInvulnerable(damageSource.getEntity(), victim, ticks, originalDamage, event.getCause());
        }
    }

    public boolean wasHitCancelled(Player player) {
        boolean wasCancelled = wasHitCancelled.getOrDefault(player, false);
        wasHitCancelled.remove(player);
        return wasCancelled;
    }
}
