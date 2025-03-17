package com.combatium.util.active;

import com.combatium.CombatiumLite;
import com.combatium.listeners.util.AnimationListener;
import com.combatium.scheduler.InvulnerabilityScheduler;
import com.combatium.util.calculations.DisableShieldUtil;
import com.combatium.util.calculations.PlayerHitUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.combatium.util.active.MaceUtil.macePostHit;
import static com.combatium.util.active.ReachUtil.resetCooldown;
import static com.combatium.util.calculations.FixItem.isSword;
import static com.combatium.util.calculations.PlayerHitUtil.getFireProt;
import static org.bukkit.Sound.ENTITY_PLAYER_ATTACK_CRIT;

public class PlayerDamageEntity {
    private static final Map<Player, Material> lastTickWeapon = new HashMap<>();
    public static final Map<Player, Boolean> hasProcessed = new HashMap<>();

    public static void playerDamageEntityEvent(Player player, LivingEntity entity, ItemStack weapon, EntityDamageEvent.DamageCause damageCause) {
        AnimationListener.validHit(player);
        weapon.damage(1, entity);

        if (CombatiumLite.hitsReduceVelocity) player.setVelocity(player.getVelocity().multiply(0.8));

        //Calculation for reduced shield blocking arc
        boolean isSwordBlocking = false;
        boolean isBlocking = false;
        Vector lookDirection = player.getLocation().subtract(entity.getLocation()).toVector();
        Vector entityDirection = entity.getLocation().getDirection();
        double angle = PlayerHitUtil.calculateAngle(new double[]{entityDirection.getX(), 0, entityDirection.getZ()}, new double[]{lookDirection.getX(), 0, lookDirection.getZ()});

        if (entity instanceof Player victim) {
            if (CombatiumLite.swordBlocking) isSwordBlocking = isSword(victim.getActiveItem().getType());
            isBlocking = victim.isBlocking();
        }

        boolean isBlocked = isBlocking && angle <= 50;
        boolean isSwordBlocked = isSwordBlocking && angle <= 50;
        if (isSwordBlocked) isBlocked = false;

        if ((isBlocked || isSwordBlocked) && DisableShieldUtil.isAxe(weapon)) { // Updated shield blocking times
            // Maybe make it so player no longer counts as blocking
            DisableShieldUtil.disableShield((Player) entity, weapon);
        }

        float damage = PlayerHitUtil.getRawDamage(player, entity, weapon, isBlocked, isSwordBlocked, damageCause);
        double invulnerableDamage = InvulnerabilityScheduler.isInvulnerable(entity);
        float originalDamage = damage;
        boolean cancelKB = false;
        damage -= (float) invulnerableDamage;
        if (damage <= 0) return;

        float finalDamage = PlayerHitUtil.getFinalDamage(entity, damage, weapon);

        if (weapon.getType() == Material.MACE) cancelKB = macePostHit(weapon, player.getFallDistance(), player, entity);

        //Apply Knockback
        Vector direction = player.getLocation().subtract(entity.getLocation()).toVector().setY(0);

        KnockbackUtil.applyKnockback(entity, (damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) ? 0.32 : 0.4000000059604645D, direction.getX(), direction.getZ(), isBlocked, isSwordBlocked);

        double strength = player.isSprinting() ? 1.0 : 0.0;

        if (damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) strength *= 0.8;

        //Increase knockback strength based on knockback enchantment level
        if (weapon.getItemMeta() != null && weapon.getItemMeta().hasEnchant(Enchantment.KNOCKBACK) && damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {

            strength += getKnockback(((CraftPlayer) player).getHandle(), ((CraftLivingEntity) entity).getHandle(),
                    ((CraftDamageSource) DamageSource.builder(DamageType.PLAYER_ATTACK).withCausingEntity(player).withDirectEntity(player).build()).getHandle(),
                    (CraftItemStack.unwrap(weapon)));
        }

        if (!cancelKB && strength > 0) {
            KnockbackUtil.applyKnockback(entity, strength * 0.5, Mth.sin(player.getLocation().getYaw() * 0.017453292F), -Mth.cos(player.getLocation().getYaw() * 0.017453292F), isBlocked, isSwordBlocked);
        }

        dealDamage(entity, damage, finalDamage, weapon, player);

        PlayerHitUtil.thornsCheck(entity, player);

        if (ReachUtil.isOverCharged(player, weapon.getType(), ReachUtil.getHaste(player)) && weapon.containsEnchantment(Enchantment.SWEEPING_EDGE) &&
                damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            SweepingEdgeUtil.Sweep(player, entity);
        }

        if (!isBlocked && weapon.containsEnchantment(Enchantment.FIRE_ASPECT)) {
            int fireProt = getFireProt(entity);
            entity.setFireTicks(fireProt > 7 ? 0 : (int) ((80 * weapon.getEnchantmentLevel(Enchantment.FIRE_ASPECT) * (1 - (0.15 * fireProt)))));
        }

        //Fix item crit particles and sounds
        if (player.getFallDistance() > 0 && !player.isInsideVehicle() && !player.isOnGround() && !player.isFlying() && damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            player.getWorld().playSound(player.getLocation(), ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
            if (player.isSprinting() || player.getAttackCooldown() > 0) {
                entity.getWorld().spawnParticle(org.bukkit.Particle.CRIT, entity.getLocation(), 50, 0.5, 1, 0.5, 0.05);
            }
        } else {
            if (player.isSprinting()) player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1, 1);
            else player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1, 1);
        }

        if (finalDamage > 2) player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, entity.getEyeLocation().add(0, -0.5, 0), (int) (finalDamage / 2), 0.1, 0.0, 0.1, 0.2);

        if (isBlocked) player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1, 1);
        //float pitchShift = (float) (Math.random() * 0.2) * ((damage > 5) ? -2 : 1);
        //if (isSwordBlocked) player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, (pitchShift + 1.3f));

        int haste = 0;
        if (player.hasPotionEffect(PotionEffectType.HASTE)) {
            haste = Objects.requireNonNull(player.getPotionEffect(PotionEffectType.HASTE)).getAmplifier();
        }

        /*
         * Set cooldown at the end to ensure things like full charge checks function properly
         */
        resetCooldown(player, false, getWeaponType(weapon.getType(), player));
        InvulnerabilityScheduler.setInvulnerable(entity, (weapon.getType() == Material.MACE) ? 10 : ReachUtil.getCooldown(weapon.getType(), haste) - 1, originalDamage);
    }

    private static void dealDamage(LivingEntity entity, float damage, float finalDamage, ItemStack weapon, Player cause) {
        hasProcessed.put(cause, true);
        DamageSource damageSource = DamageSource.builder(DamageType.PLAYER_ATTACK).withCausingEntity(cause).withDirectEntity(cause).withDamageLocation(cause.getLocation()).build();
        entity.damage(damage, damageSource);
    }

    private static boolean isKillShot(LivingEntity entity, float finalDamage) {
        if (entity.getActiveItem().getType() == Material.TOTEM_OF_UNDYING) {
            return false;
        }
        if (entity instanceof Player player) {
            PlayerInventory inv = player.getInventory();
            if (inv.getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING || inv.getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) {
                return false;
            }
        }
        return entity.getHealth() - finalDamage <= 0;
    }

    protected static float getKnockback(net.minecraft.world.entity.LivingEntity attacker, Entity target, net.minecraft.world.damagesource.DamageSource damageSource, net.minecraft.world.item.ItemStack item) {
        float f = (float) attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK);

        return EnchantmentHelper.modifyKnockback((ServerLevel) attacker.level(), item, target, damageSource, f);
    }

    private static Material getWeaponType(Material material, Player player) {
        if (CombatiumLite.swapAccuracy && lastTickWeapon.containsKey(player)) {
            return lastTickWeapon.get(player);
        }
        return material;
    }

    public static void setLastTickWeapon(Player player, Material material) {
        lastTickWeapon.put(player, material);
    }

    public static boolean hasProcessed(Player player) {
        if (!hasProcessed.containsKey(player)) {
            hasProcessed.put(player, false);
        }
        return hasProcessed.get(player);
    }
}
