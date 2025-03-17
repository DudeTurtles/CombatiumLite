package com.combatium.util.active;

import com.combatium.CombatiumLite;
import com.combatium.listeners.util.AnimationListener;
import com.combatium.scheduler.InvulnerabilityScheduler;
import com.combatium.util.calculations.DisableShieldUtil;
import com.combatium.util.calculations.PlayerHitUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.bukkit.*;
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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.combatium.CombatiumLite.sprintFix;
import static com.combatium.util.active.MaceUtil.macePostHit;
import static com.combatium.util.active.ReachUtil.resetCooldown;
import static com.combatium.util.calculations.PlayerHitUtil.*;
import static org.bukkit.Sound.ENTITY_PLAYER_ATTACK_CRIT;

public class PlayerDamageEntity {
    private static final Map<Player, ItemStack> lastTickWeapon = new HashMap<>();
    public static final Map<Player, Boolean> hasProcessed = new HashMap<>();
    public static final NamespacedKey BLOCK_ANGLE = new NamespacedKey("combatium", "block_angle");
    public static final NamespacedKey BLOCK_AMOUNT = new NamespacedKey("combatium", "block_amount");
    public static final NamespacedKey KNOCKBACK_REDUCTION = new NamespacedKey("combatium", "knockback_reduction");

    public static double playerDamageEntityEvent(Player player, LivingEntity entity, ItemStack weapon, EntityDamageEvent.DamageCause damageCause, boolean dealDamage) {
        AnimationListener.validHit(player);
        weapon.damage(1, entity);

        if (CombatiumLite.hitsReduceVelocity) player.setVelocity(player.getVelocity().multiply(0.8));

        //Calculation for reduced shield blocking arc
        net.minecraft.world.entity.Entity nmsEntity = ((CraftLivingEntity) entity).getHandle();

        boolean isBlocked = false;
        double blockAmount = 0;
        double knockbackReduction = 0;
        double blockAngle = 0;

        if (entity instanceof Player p && p.isBlocking()) {
            double angle = getAngle(player, entity);
            ItemStack shield = p.getActiveItem();

            if (shield.getItemMeta() != null) {
                blockAmount = shield.getItemMeta().getPersistentDataContainer().getOrDefault(BLOCK_AMOUNT, PersistentDataType.DOUBLE, 5.0);
                knockbackReduction = shield.getItemMeta().getPersistentDataContainer().getOrDefault(KNOCKBACK_REDUCTION, PersistentDataType.DOUBLE, 0.5);
                blockAngle = shield.getItemMeta().getPersistentDataContainer().getOrDefault(BLOCK_ANGLE, PersistentDataType.DOUBLE, CombatiumLite.shieldProtectionArc);
            } else {
                blockAmount = 5;
                knockbackReduction = 0.5;
                blockAngle = CombatiumLite.shieldProtectionArc;
            }

            isBlocked = (angle < Math.toRadians(blockAngle - 180)) || blockAngle >= 360;
        }

        if ((isBlocked) && DisableShieldUtil.isAxe(weapon)) { // Updated shield blocking times
            // Maybe make it so player no longer counts as blocking
            DisableShieldUtil.disableShield((Player) entity, weapon);
        }

        float damage = PlayerHitUtil.getRawDamage(player, entity, weapon, isBlocked, blockAmount, damageCause);
        double invulnerableDamage = InvulnerabilityScheduler.isInvulnerable(entity);
        float originalDamage = damage;
        boolean cancelKB = false;
        damage -= (float) invulnerableDamage;
        if (damage <= 0) return 0;

        float finalDamage = PlayerHitUtil.getFinalDamage(entity, damage, weapon);

        if (weapon.getType() == Material.MACE) cancelKB = macePostHit(weapon, player.getFallDistance(), player, entity);

        double strength;
        //Apply Knockback
        if (dealDamage) {
            strength = player.isSprinting() ? 1.0 : 0.0;

            //Increase knockback strength based on knockback enchantment level
            if (weapon.getItemMeta() != null && weapon.getItemMeta().hasEnchant(Enchantment.KNOCKBACK) && damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {

                strength += getKnockback(((CraftPlayer) player).getHandle(), ((CraftLivingEntity) entity).getHandle(),
                        ((CraftDamageSource) DamageSource.builder(DamageType.PLAYER_ATTACK).withCausingEntity(player).withDirectEntity(player).build()).getHandle(),
                        (CraftItemStack.unwrap(weapon)));
            }
        } else {
            strength = (player.isSprinting() && (player.getAttackCooldown() <= 0.9)) ? 1.0 : 0.0;
        }

        if (damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) KnockbackUtil.applyKnockback(entity, 0.4f, Mth.sin(player.getLocation().getYaw() * 0.017453292F), -Mth.cos(player.getLocation().getYaw() * 0.017453292F), isBlocked, knockbackReduction, false);

        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

        //Fix item crit particles and sounds
        if (player.getFallDistance() > 0 && !player.isInsideVehicle() && !player.isOnGround() && !player.isFlying() && damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            player.getWorld().playSound(player.getLocation(), ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
            if (player.isSprinting() || player.getAttackCooldown() > 0) {
                nmsPlayer.serverLevel().getChunkSource().broadcastAndSend(nmsEntity, new net.minecraft.network.protocol.game.ClientboundAnimatePacket(nmsEntity, 4));
            }
        } else {
            if (player.isSprinting()) player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1, 1);
            else player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1, 1);
        }

        if (dealsEnchantDamage(weapon, entity)) nmsPlayer.serverLevel().getChunkSource().broadcastAndSend(nmsEntity, new net.minecraft.network.protocol.game.ClientboundAnimatePacket(nmsEntity, 5));

        if (finalDamage > 2) player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, entity.getEyeLocation().add(0, -0.5, 0), (int) (finalDamage / 2), 0.1, 0.0, 0.1, 0.2);

        if (isBlocked) player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1, 1);
        //float pitchShift = (float) (Math.random() * 0.2) * ((damage > 5) ? -2 : 1);
        //if (isSwordBlocked) player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, (pitchShift + 1.3f));

        if (dealDamage) dealDamage(entity, damage, player);

        if (!cancelKB && strength > 0 && !(damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            KnockbackUtil.applyKnockback(entity, strength * 0.5, Mth.sin(player.getLocation().getYaw() * 0.017453292F), -Mth.cos(player.getLocation().getYaw() * 0.017453292F), isBlocked, knockbackReduction, false);
        }

        if (ReachUtil.isOverCharged(player, weapon, ReachUtil.getHaste(player)) && weapon.containsEnchantment(Enchantment.SWEEPING_EDGE) &&
                damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            SweepingEdgeUtil.Sweep(player, entity);
        }

        PlayerHitUtil.thornsCheck(entity, player);

        if (!isBlocked && weapon.containsEnchantment(Enchantment.FIRE_ASPECT)) {
            int fireProt = getFireProt(entity);
            entity.setFireTicks(fireProt > 7 ? 0 : (int) ((80 * weapon.getEnchantmentLevel(Enchantment.FIRE_ASPECT) * (1 - (0.15 * fireProt)))));
        }

        int haste = 0;
        if (player.hasPotionEffect(PotionEffectType.HASTE)) {
            haste = Objects.requireNonNull(player.getPotionEffect(PotionEffectType.HASTE)).getAmplifier();
        }

        /*
         * Set cooldown at the end to ensure things like full charge checks function properly. Trust me, I know it's a little
         * stupid, but it works. Since weapon swapping uses the cooldown of the weapon you swapped from, this is necessary to
         * retrieve the correct cooldown. If that is disabled, this is definitely inefficient, but not in any significant way.
         */
        resetCooldown(player, false, getWeaponType(weapon, player));
        InvulnerabilityScheduler.setInvulnerable(entity, ReachUtil.getCooldown(weapon, haste) - 2, originalDamage);
        return damage;
    }

    private static void dealDamage(LivingEntity entity, float damage, Player cause) {
        hasProcessed.put(cause, true);
        ServerPlayer nmsPlayer = ((CraftPlayer) cause).getHandle();
        boolean sprinting = nmsPlayer.isSprinting();
        DamageSource damageSource = DamageSource.builder(DamageType.PLAYER_ATTACK).withCausingEntity(cause).withDirectEntity(cause).withDamageLocation(cause.getLocation()).build();
        entity.damage(damage, damageSource);

        if (!sprintFix) {
            nmsPlayer.setSprinting(false);
        } else {
            nmsPlayer.setSprinting(sprinting);
        }

        cause.incrementStatistic(Statistic.DAMAGE_DEALT, Math.round(damage * 10));
    }

    protected static float getKnockback(net.minecraft.world.entity.LivingEntity attacker, Entity target, net.minecraft.world.damagesource.DamageSource damageSource, net.minecraft.world.item.ItemStack item) {
        float f = (float) attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK);

        return EnchantmentHelper.modifyKnockback((ServerLevel) attacker.level(), item, target, damageSource, f);
    }

    private static ItemStack getWeaponType(ItemStack itemStack, Player player) {
        if (CombatiumLite.swapAccuracy && lastTickWeapon.containsKey(player)) {
            return lastTickWeapon.get(player);
        }
        return itemStack;
    }

    public static void setLastTickWeapon(Player player, ItemStack item) {
        lastTickWeapon.put(player, item);
    }

    public static boolean hasProcessed(Player player) {
        if (!hasProcessed.containsKey(player)) {
            hasProcessed.put(player, false);
        }
        return hasProcessed.get(player);
    }
}
