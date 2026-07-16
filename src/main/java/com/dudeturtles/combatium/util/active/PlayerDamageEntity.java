package com.dudeturtles.combatium.util.active;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.listeners.damage.EntityDamageByEntityListener;
import com.dudeturtles.combatium.listeners.util.AnimationListener;
import com.dudeturtles.combatium.util.calculations.PlayerHitUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerDamageEntity {
    public static final NamespacedKey KNOCKBACK_REDUCTION = new NamespacedKey("combatium", "knockback_reduction");
    private static final Map<Player, ItemStack> lastTickWeapon = new HashMap<>();
    private static EntityDamageByEntityListener entityDamageByEntityListener;

    public static boolean playerDamageEntityEvent(Player player, LivingEntity target, ItemStack weapon, EntityDamageEvent.DamageCause damageCause, boolean dealDamage) {
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

        if (target.isInvulnerable() || target.isDead()) return false;
        if (target instanceof Player p && (p.getGameMode().isInvulnerable() || !nmsPlayer.canHarmPlayer(((CraftPlayer) p).getHandle())))
            return false;

        net.minecraft.world.item.ItemStack nmsWeapon = CraftItemStack.unwrap(weapon);

        //Calculation for reduced shield blocking arc
        net.minecraft.world.damagesource.DamageSource damageSource = nmsWeapon.getDamageSource(nmsPlayer);
        float damage = PlayerHitUtil.getRawDamage(player, target, weapon, damageCause, damageSource, isCrit);

        double invulnerableDamage = CombatiumLite.getScheduler().getInvulnerabilityScheduler().isInvulnerable(target);
        float originalDamage = damage;
        damage -= (float) invulnerableDamage;
        if (damage <= 0) {
            nmsPlayer.postPiercingAttack();
            return false;
        }

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
        if (dealDamage) dealDamage(target, damage, player, damageSource);
        if (entityDamageByEntityListener.wasHitCancelled(player)) {
            nmsPlayer.postPiercingAttack();
            return false;
        }
        causeExtraKnockback(nmsPlayer, nmsTarget, initialTargetDelta, false, knockbackStrength, damageSource, damage, true);
        attackVisualEffects(nmsPlayer, nmsTarget, isCrit, damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, PlayerHitUtil.dealsEnchantDamage(weapon, nmsPlayer, target));
        nmsPlayer.setLastHurtMob(nmsTarget);
        itemAttackInteraction(nmsPlayer, nmsTarget, nmsWeapon, damageSource, true);
        damageStatsAndHearts(nmsPlayer, nmsTarget, (float) oldHealth);
        nmsPlayer.causeFoodExhaustion(nmsPlayer.level().spigotConfig.combatExhaustion, EntityExhaustionEvent.ExhaustionReason.ATTACK);

        if (damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
            KnockbackUtil.applyKnockback(target, 0.4f, Mth.sin(player.getLocation().getYaw() * 0.017453292F), -Mth.cos(player.getLocation().getYaw() * 0.017453292F), false, damageSource, damage, false);

        if (CombatiumLite.getReachUtil().isOverCharged(player, 1.0F) && SweepingEdgeUtil.hasSweeping(player) &&
                damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            SweepingEdgeUtil.sweepAttack(player, target);
        }

        /*
         * Set cooldown at the end to ensure things like full charge checks function properly. Trust me, I know it's a little
         * stupid, but it works. Since weapon swapping uses the cooldown of the weapon you swapped from, this is necessary to
         * retrieve the correct cooldown. If that is disabled, this is definitely inefficient, but not in any significant way.
         */
        // Note: This is literally just how it works in CTS though??
        if (damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
            CombatiumLite.getReachUtil().resetCooldown(player, false);
        CombatiumLite.getScheduler().getInvulnerabilityScheduler().setInvulnerable(nmsPlayer, target, damageSource.is(DamageTypeTags.IS_PROJECTILE) ? 0 : Mth.clamp(CombatiumLite.getReachUtil().getCooldown(player) - 3, 0, 10), originalDamage, damageCause);
        nmsPlayer.postPiercingAttack();
        return true;
    }

    public static void causeExtraKnockback(net.minecraft.world.entity.player.Player nmsPlayer, net.minecraft.world.entity.Entity nmsTarget, Vec3 initialTargetDelta, boolean cancelKB, double strength, DamageSource damageSource, float damage, boolean comesFromEffect) {
        if (!cancelKB && strength > 0.0F) {
            if (nmsTarget.getBukkitEntity() instanceof LivingEntity livingEntity) {
                KnockbackUtil.applyKnockback(livingEntity, strength * 0.5, Mth.sin(nmsPlayer.getYRot() * ((float)Math.PI / 180F)), -Mth.cos(nmsPlayer.getYRot() * ((float)Math.PI / 180F)), false, damageSource, damage, comesFromEffect);
            } else {
                nmsTarget.push(-Mth.sin(nmsPlayer.getYRot() * ((float)Math.PI / 180F)) * strength, 0.1, Mth.cos(nmsPlayer.getYRot() * ((float)Math.PI / 180F)) * strength, nmsPlayer);
            }

            nmsPlayer.setDeltaMovement(nmsPlayer.getDeltaMovement().multiply(0.6, 1.0F, 0.6));
            if (!nmsPlayer.level().paperConfig().misc.disableSprintInterruptionOnAttack) {
                nmsPlayer.setSprinting(false);
            }
        }

        if (nmsTarget instanceof ServerPlayer targetPlayer && targetPlayer.hurtMarked) {
            targetPlayer.connection.send(new ClientboundSetEntityMotionPacket(targetPlayer));
            targetPlayer.hurtMarked = false;
            targetPlayer.setDeltaMovement(initialTargetDelta);
        }
    }

    public static void dealDamage(LivingEntity entity, float damage, Player cause, net.minecraft.world.damagesource.DamageSource damageSource) {
        try {
            entity.damage(damage, new CraftDamageSource(damageSource));
        } catch (Exception e) {
            CombatiumLite.getPluginLogger().warning("Failed to deal damage to entity: " + entity.getName());
            CombatiumLite.getPluginLogger().warning("Error Message: " + e.getMessage());
        }
    }

    protected static float getKnockback(net.minecraft.world.entity.LivingEntity attacker, Entity target, net.minecraft.world.damagesource.DamageSource damageSource, net.minecraft.world.item.ItemStack item) {
        float knockback = (float) attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK);

        return EnchantmentHelper.modifyKnockback((ServerLevel) attacker.level(), item, target, damageSource, knockback);
    }

    public static ItemStack getWeaponType(ItemStack itemStack, Player player) {
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

    public static void attackVisualEffects(net.minecraft.world.entity.player.Player nmsPlayer, Entity nmsTarget, boolean isCrit, boolean isSweep, boolean dealsEnchantDamage) {
        if (dealsEnchantDamage)
            nmsPlayer.magicCrit(nmsTarget);
        if (isCrit) {
            playServerSideSound(nmsPlayer, SoundEvents.PLAYER_ATTACK_CRIT);
            nmsPlayer.crit(nmsTarget);
        }

        if (!isCrit && !isSweep)
            playServerSideSound(nmsPlayer, SoundEvents.PLAYER_ATTACK_STRONG);
    }

    public static void itemAttackInteraction(net.minecraft.world.entity.player.Player nmsPlayer, Entity target, net.minecraft.world.item.ItemStack stack, net.minecraft.world.damagesource.DamageSource damageSource, boolean postAttackEffects) {
        Entity entity = target;
        if (target instanceof EnderDragonPart) {
            entity = ((EnderDragonPart)target).parentMob;
        }

        boolean canGoToPost = false;
        Level var8 = nmsPlayer.level();
        if (var8 instanceof ServerLevel serverLevel) {
            if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity)
                canGoToPost = stack.hurtEnemy(livingEntity, nmsPlayer);

            if (postAttackEffects)
                EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, target, damageSource, stack);
        }

        if (!nmsPlayer.level().isClientSide() && !stack.isEmpty() && entity instanceof net.minecraft.world.entity.LivingEntity) {
            if (canGoToPost) stack.postHurtEnemy((net.minecraft.world.entity.LivingEntity)entity, nmsPlayer);

            if (stack.isEmpty()) {
                if (stack == nmsPlayer.getMainHandItem())
                    nmsPlayer.setItemInHand(InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
                else
                    nmsPlayer.setItemInHand(InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
    }
    public static void playServerSideSound(net.minecraft.world.entity.player.Player fromEntity, SoundEvent soundEvent) {
        sendSoundEffect(fromEntity, fromEntity.getX(), fromEntity.getY(), fromEntity.getZ(), soundEvent, fromEntity.getSoundSource(), 1.0F, 1.0F);
    }
    public static void sendSoundEffect(net.minecraft.world.entity.player.Player fromEntity, double x, double y, double z, SoundEvent soundEffect, SoundSource soundCategory, float volume, float pitch) {
        fromEntity.level().playSound(fromEntity, x, y, z, soundEffect, soundCategory, volume, pitch);
        if (fromEntity instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEffect), soundCategory, x, y, z, volume, pitch, fromEntity.getRandom().nextLong()));
        }
    }

    public static void damageStatsAndHearts(net.minecraft.world.entity.player.Player nmsPlayer, Entity target, float previousHealth) {
        if (target instanceof net.minecraft.world.entity.LivingEntity) {
            float dealtDamage = previousHealth - ((net.minecraft.world.entity.LivingEntity)target).getHealth();
            nmsPlayer.awardStat(Stats.DAMAGE_DEALT, Math.round(dealtDamage * 10.0F));
            if (nmsPlayer.level() instanceof ServerLevel && dealtDamage > 2.0F) {
                int count = (int)((double)dealtDamage * (double)0.5F);
                ((ServerLevel)nmsPlayer.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5F), target.getZ(), count, 0.1, (double)0.0F, 0.1, 0.2);
            }
        }
    }
}