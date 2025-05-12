package com.combatium.listeners.damage;

import com.combatium.CombatiumLite;
import com.combatium.scheduler.InvulnerabilityScheduler;
import com.combatium.util.active.ReachUtil;
import com.combatium.util.calculations.PlayerHitUtil;
import org.bukkit.Material;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;

public class EntityDamageByEntityListener implements Listener {
    private final InvulnerabilityScheduler invulnerabilityScheduler;
    private final HashMap<Player, Boolean> wasHitCancelled = new HashMap<>();
    private final ReachUtil reachUtil;

    public EntityDamageByEntityListener() {
        this.invulnerabilityScheduler = CombatiumLite.getScheduler().getInvulnerabilityScheduler();
        this.reachUtil = CombatiumLite.getReachUtil();
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

        if (event.getDamager() instanceof Player player) {
            if (event.getEntity() instanceof LivingEntity e && (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < 0)){
                if (event.getEntity() instanceof Player && (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < 0)) {
                    event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, 0);
                    event.setDamage(PlayerHitUtil.getFinalDamage(e, (float) event.getDamage(), player.getInventory().getItemInMainHand()));
                    if (e.getAbsorptionAmount() > 0) event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, -1 * Math.min(e.getAbsorptionAmount(), event.getDamage()));
                }
            }
        } else if (event.getDamager() instanceof Trident trident) { // Updated trident damage calculation
            double damage = 8;
            if (trident.getItemStack().containsEnchantment(Enchantment.IMPALING) && (event.getEntity()).isInWaterOrRain()) {
                damage += trident.getItemStack().getEnchantmentLevel(Enchantment.IMPALING) * 2.5;
            }
            event.setDamage(damage);
        } else if (event.getEntity() instanceof LivingEntity victim) { // Ensure replacement i-frames work for other cases
            double invulDamage = invulnerabilityScheduler.isInvulnerable(victim);

            if (invulDamage >= event.getDamage()) {
                event.setCancelled(true);
                event.setDamage(0);
                return;
            }

            if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < -5) {
                event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, 0);
                event.setDamage(PlayerHitUtil.getFinalDamage(victim, (float) event.getDamage() - 5, new ItemStack(Material.AIR)));
                if (victim.getAbsorptionAmount() > 0) event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, -1 * Math.min(victim.getAbsorptionAmount(), event.getDamage()));
            }

            double originalDamage = event.getDamage();
            event.setDamage(event.getDamage() - invulDamage);

            if (!(event.getDamager() instanceof Projectile)) { // Ensure i-frames are set when damage is not dealt by a projectile
                int ticks;
                if (event.getDamager() instanceof LivingEntity de && de.getActiveItem().getType() != Material.AIR) {
                    ticks = reachUtil.getCooldown(de);
                } else {
                    ticks = 10;
                }

                invulnerabilityScheduler.setInvulnerable(victim, ticks, originalDamage, event.getCause());
            }
        }
    }

    public boolean wasHitCancelled(Player player) {
        boolean wasCancelled = wasHitCancelled.getOrDefault(player, false);
        wasHitCancelled.remove(player);
        return wasCancelled;
    }
}
