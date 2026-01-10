package com.dudeturtles.combatium.util.active;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.listeners.damage.EntityDamageByEntityListener;
import com.dudeturtles.combatium.listeners.util.AnimationListener;
import com.dudeturtles.combatium.util.calculations.PlayerHitUtil;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerDamageEntity {
    public static final NamespacedKey KNOCKBACK_REDUCTION = new NamespacedKey("combatium", "knockback_reduction");
    private static final Map<Player, ItemStack> lastTickWeapon = new HashMap<>();
    private static EntityDamageByEntityListener entityDamageByEntityListener;

    public static void playerDamageEntityEvent(Player player, LivingEntity target, ItemStack weapon, EntityDamageEvent.DamageCause damageCause, boolean dealDamage) {
        AnimationListener.validHit(player);
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.world.entity.LivingEntity nmsTarget = ((CraftLivingEntity) target).getHandle();

        //Fix item crit particles and sounds
        double knockbackStrength = 0;
        boolean canDoSpecial = !nmsPlayer.onClimbable()
                && !nmsPlayer.isInWater()
                && !nmsPlayer.hasEffect(MobEffects.BLINDNESS)
                && !nmsPlayer.isPassenger();
        boolean isCrit;
        if (player.isSprinting() && canDoSpecial) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1, 1);
            knockbackStrength += 1;
        }
        isCrit = canDoSpecial && player.getFallDistance() > 0 && !player.isOnGround() && damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK;

        if (PlayerHitUtil.dealsEnchantDamage(weapon, nmsPlayer, target)) {
            try {
                nmsPlayer.level().getChunkSource().sendToTrackingPlayers(nmsTarget, new net.minecraft.network.protocol.game.ClientboundAnimatePacket(nmsTarget, 5));
            } catch (NoSuchMethodError ignored) {
                CombatiumLite.logger.severe("Error Sending Enchant Damage Particle Packet");
            }
        }

        if (target.isInvulnerable() || target.isDead()) return;
        if (target instanceof Player p && (p.getGameMode().isInvulnerable() || !nmsPlayer.canHarmPlayer(((CraftPlayer) p).getHandle())))
            return;

        net.minecraft.world.item.ItemStack nmsWeapon = CraftItemStack.asNMSCopy(weapon);

        // For some reason this would occasionally cause issues when dealing with players hitting bot players created by other plugins
        if (weapon != null && weapon.getType() != Material.AIR && !player.getGameMode().isInvulnerable()) {
            try {
                DataComponentMap map = nmsWeapon.getComponents();
                Weapon component = map.has(DataComponents.WEAPON) ? map.get(DataComponents.WEAPON) : null;
                weapon.damage((component == null) ? 0 : component.itemDamagePerAttack(), target);
            } catch (Exception ignored) {
                CombatiumLite.getPluginLogger().warning("Failed to damage weapon");
                CombatiumLite.getPluginLogger().warning("Error Message: " + ignored.getMessage());
            }
        }

        //Calculation for reduced shield blocking arc
        float damage = PlayerHitUtil.getRawDamage(player, target, weapon, damageCause, isCrit);
        net.minecraft.world.damagesource.DamageSource damageSource = Optional.ofNullable(nmsWeapon.getItem().getDamageSource(nmsPlayer)).orElse(nmsPlayer.damageSources().playerAttack(nmsPlayer));

        double invulnerableDamage = CombatiumLite.getScheduler().getInvulnerabilityScheduler().isInvulnerable(target);
        float originalDamage = damage;
        boolean cancelKB = false;
        damage -= (float) invulnerableDamage;
        if (damage <= 0) return;

        //Apply Knockback
        if (dealDamage) {
            //Increase knockback strength based on knockback enchantment level
            if (weapon.getItemMeta() != null && weapon.getItemMeta().hasEnchant(Enchantment.KNOCKBACK) && damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                knockbackStrength += getKnockback(((CraftPlayer) player).getHandle(), ((CraftLivingEntity) target).getHandle(),
                        damageSource,
                        (CraftItemStack.unwrap(weapon)));
            }
        }

        double oldHealth = target.getHealth();
        Vec3 initialTargetDelta = nmsTarget.getDeltaMovement();
        if (dealDamage) dealDamage(target, damage, player);
        double finalDamage = oldHealth - target.getHealth();
        if (entityDamageByEntityListener.wasHitCancelled(player)) return;

        if (weapon.getType() == Material.MACE)
            cancelKB = MaceUtil.macePostHit(weapon, player.getFallDistance(), player, target);
        EnchantmentHelper.doPostAttackEffectsWithItemSource(nmsPlayer.level(), nmsTarget, damageSource, nmsWeapon);

        if (damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
            KnockbackUtil.applyKnockback(target, 0.4f, Mth.sin(player.getLocation().getYaw() * 0.017453292F), -Mth.cos(player.getLocation().getYaw() * 0.017453292F), false);

        if (PlayerHitUtil.dealsEnchantDamage(weapon, nmsPlayer, target)) {
            try {
                nmsPlayer.level().getChunkSource().sendToTrackingPlayers(nmsTarget, new ClientboundAnimatePacket(nmsTarget, 5));
            } catch (NoSuchMethodError ignored) {
                CombatiumLite.logger.severe("Error Sending Enchant Damage Particle Packet");
            }
        }
        if (isCrit) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
            try {
                nmsPlayer.level().getChunkSource().sendToTrackingPlayers(nmsTarget, new ClientboundAnimatePacket(nmsTarget, 4));
            } catch (NoSuchMethodError ignored) {
                CombatiumLite.logger.severe("Error Sending Crit Particle Packet");
            }
        }

        if (!isCrit && !(damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK))
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1, 1);

        if (finalDamage > 2)
            player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getEyeLocation().add(0, -0.5, 0), (int) (finalDamage / 2), 0.1, 0.0, 0.1, 0.2);

        if (!cancelKB && knockbackStrength > 0 && !(damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            KnockbackUtil.applyKnockback(target, knockbackStrength * 0.5, Mth.sin(player.getLocation().getYaw() * 0.017453292F), -Mth.cos(player.getLocation().getYaw() * 0.017453292F), false);
        }

        if (CombatiumLite.getReachUtil().isOverCharged(player, 1.0F) && weapon.containsEnchantment(Enchantment.SWEEPING_EDGE) &&
                damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            SweepingEdgeUtil.Sweep(player, target);
        }

        /*
         * Set cooldown at the end to ensure things like full charge checks function properly. Trust me, I know it's a little
         * stupid, but it works. Since weapon swapping uses the cooldown of the weapon you swapped from, this is necessary to
         * retrieve the correct cooldown. If that is disabled, this is definitely inefficient, but not in any significant way.
         */
        // Note: This is literally just how it works in CTS though??
        if (damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
            CombatiumLite.getReachUtil().resetCooldown(player, false);
        CombatiumLite.getScheduler().getInvulnerabilityScheduler().setInvulnerable(target, CombatiumLite.getReachUtil().getCooldown(player) - 3, originalDamage, damageCause);
        if (nmsTarget instanceof ServerPlayer targetPlayer && targetPlayer.hurtMarked) {
            targetPlayer.connection.send(new ClientboundSetEntityMotionPacket(targetPlayer));
            targetPlayer.hurtMarked = false;
            targetPlayer.setDeltaMovement(initialTargetDelta);
        }
    }

    private static void dealDamage(LivingEntity entity, float damage, Player cause) {
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) cause).getHandle();
            boolean sprinting = nmsPlayer.isSprinting();
            DamageSource damageSource = DamageSource.builder(DamageType.PLAYER_ATTACK).withCausingEntity(cause).withDirectEntity(cause).withDamageLocation(cause.getLocation()).build();
            entity.damage(damage, damageSource);

            if (!CombatiumLite.sprintFix) {
                nmsPlayer.setSprinting(false);
            } else {
                nmsPlayer.setSprinting(sprinting);
            }

            cause.incrementStatistic(Statistic.DAMAGE_DEALT, Math.round(damage * 10));
        } catch (Exception e) {
            CombatiumLite.getPluginLogger().warning("Failed to deal damage to entity: " + entity.getName());
            CombatiumLite.getPluginLogger().warning("Error Message: " + e.getMessage());
        }
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

    public static void setEntityDamageByEntityListener(EntityDamageByEntityListener entityDamageByEntityListener) {
        PlayerDamageEntity.entityDamageByEntityListener = entityDamageByEntityListener;
    }
}