package com.dudeturtles.combatium.util.calculations;

import com.dudeturtles.combatium.CombatiumLite;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class PlayerHitUtil {
    private static Boolean backShots;
    private static final Map<Player, Double> lastTickDamage = new HashMap<>();
    private static final NamespacedKey enchantedDamageBonus = new NamespacedKey("combatium", "magic_damage_bonus");

    public static float getRawDamage(Player attacker, Entity damagedEntity, ItemStack weapon, EntityDamageEvent.DamageCause damageCause) {
        float damage = (float) attackDamage(attacker);
        float enchantedDamage;

        ItemMeta meta = weapon.getItemMeta();
        if (meta != null && meta.hasEnchants()) {
            ServerPlayer player = ((CraftPlayer)attacker).getHandle();
            enchantedDamage = EnchantmentHelper.modifyDamage(player.level(), CraftItemStack.asNMSCopy(weapon), ((CraftEntity)damagedEntity).getHandle(), player.damageSources().playerAttack(player), damage) - damage;
            enchantedDamage = getImpalingDamage(player, damagedEntity, meta, enchantedDamage);
            if (enchantedDamage != 0) {
                damage = (float) enchantedDamage(attacker, enchantedDamage);
            }
            if (meta.hasEnchant(Enchantment.SWEEPING_EDGE) && damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                float sweepLevel = meta.getEnchantLevel(Enchantment.SWEEPING_EDGE);
                return 1 + (damage * /*player.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO)*/(sweepLevel / (2f * (sweepLevel + 1f))));
            } else if (damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                return 0;
            }
        }

        if (weapon.getType() == Material.MACE) {
            net.minecraft.world.item.ItemStack nmsWeapon = CraftItemStack.asNMSCopy(weapon);
            net.minecraft.world.entity.LivingEntity nmsEntity = ((CraftLivingEntity) damagedEntity).getHandle();
            net.minecraft.world.entity.LivingEntity attackerNMS = ((CraftLivingEntity) attacker).getHandle();
            damage += nmsWeapon.getItem().getAttackDamageBonus(nmsEntity, damage, attackerNMS.damageSources().playerAttack((net.minecraft.world.entity.player.Player) attackerNMS));
        }

        if (attacker.getFallDistance() > 0 && !attacker.isInsideVehicle() && !attacker.isOnGround() && !attacker.isFlying() && (damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            damage *= (CombatiumLite.getReachUtil().isOverCharged(attacker, 0.0F) || !CombatiumLite.weakerSpamCrits) ? 1.5f : 1.25f;
        }
        if (backShots && wasBackShots(attacker, damagedEntity)) {
            damage *= 1.5f;
        }

        return damage;
    }

    public static boolean dealsEnchantDamage(ItemStack weapon, ServerPlayer player, Entity damagedEntity) {
        if (!(weapon.getItemMeta() != null && weapon.getItemMeta().hasEnchants())) return false;
        float damage = (float) attackDamage(player.getBukkitEntity());
        ItemMeta meta = weapon.getItemMeta();
        float enchantedDamage = EnchantmentHelper.modifyDamage(player.level(), CraftItemStack.asNMSCopy(weapon), ((CraftEntity)damagedEntity).getHandle(), player.damageSources().playerAttack(player), damage) - damage;
        enchantedDamage = getImpalingDamage(player, damagedEntity, meta, enchantedDamage);
        return enchantedDamage != 0;
    }

    private static float getImpalingDamage(ServerPlayer player, Entity damagedEntity, ItemMeta meta, float enchantedDamage) {
        Holder.Reference<net.minecraft.world.item.enchantment.Enchantment> impaling = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(ResourceLocation.withDefaultNamespace("impaling")).orElse(null);
        if (meta.hasEnchant(Enchantment.IMPALING) && damagedEntity.isInWaterOrRain() && (impaling == null || impaling.value()
                .getEffects(EnchantmentEffectComponents.DAMAGE).stream().noneMatch(conditionalEffect -> {
                    LootContext context = net.minecraft.world.item.enchantment.Enchantment.damageContext(player.level(), meta.getEnchantLevel(Enchantment.IMPALING), ((CraftEntity)damagedEntity).getHandle(), player.damageSources().playerAttack(player));
                    return conditionalEffect.matches(context);
                }))) enchantedDamage += (meta.getEnchantLevel(Enchantment.IMPALING) * 2.5f);
        return enchantedDamage;
    }

    public static boolean wasBackShots(Player attacker, Entity damagedEntity) {
        double angle = getAngle(attacker, damagedEntity);
        return angle >= Math.toRadians(100);
    }

    public static double getAngle(Player attacker, Entity damagedEntity) {
        Vec3 vector = CraftLocation.toVec3(attacker.getLocation());
        net.minecraft.world.entity.Entity nmsEntity = ((CraftLivingEntity) damagedEntity).getHandle();
        Vec3 balls = nmsEntity.calculateViewVector(0.0f, nmsEntity.getYHeadRot());
        Vec3 directionVector = vector.vectorTo(nmsEntity.position());
        directionVector = directionVector.subtract(0, directionVector.y(), 0).normalize();
        return directionVector.dot(balls) * Math.PI;
    }

    public static double attackDamage(Player attacker) {
        if (attacker.isRiptiding()) {
            try {
                Field autoSpinAttack = net.minecraft.world.entity.LivingEntity.class.getDeclaredField("autoSpinAttackDmg");
                autoSpinAttack.setAccessible(true);
                return autoSpinAttack.getFloat(((CraftPlayer) attacker).getHandle());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                CombatiumLite.getPluginLogger().log(Level.SEVERE, "Failed to retrieve riptide damage!" + e, e);
            }
        }
        if (CombatiumLite.swapAccuracy && lastTickDamage.containsKey(attacker)) return lastTickDamage.get(attacker);

        return Objects.requireNonNull(attacker.getAttribute(Attribute.ATTACK_DAMAGE)).getValue();
    }

    /*
     * The dam and val variables serve to ensure weapon swapping works even if the weapon has enchantments
     */
    private static double enchantedDamage(Player attacker, float enchantedDamage) {
        if (attacker.isRiptiding()) return enchantedDamage + attackDamage(attacker);
        double dam = attackDamage(attacker);
        var attrib = Objects.requireNonNull(attacker.getAttribute(Attribute.ATTACK_DAMAGE));
        double val = attrib.getValue();
        attrib.addTransientModifier(new AttributeModifier(enchantedDamageBonus, enchantedDamage, AttributeModifier.Operation.ADD_NUMBER));
        double res = attrib.getValue();
        attrib.removeModifier(enchantedDamageBonus);
        return res - val + dam;
    }

    public static void setBackShots(Boolean backShots) {
        PlayerHitUtil.backShots = backShots;
    }

    public static int getFireProt(LivingEntity entity) {
        ItemStack helmet = null;
        ItemStack chestplate = null;
        ItemStack leggings = null;
        ItemStack boots = null;

        if (entity instanceof Player player) {
            helmet = player.getInventory().getHelmet();
            chestplate = player.getInventory().getChestplate();
            leggings = player.getInventory().getLeggings();
            boots = player.getInventory().getBoots();
        } else if (entity.getEquipment() != null) {
            helmet = entity.getEquipment().getHelmet();
            chestplate = entity.getEquipment().getChestplate();
            leggings = entity.getEquipment().getLeggings();
            boots = entity.getEquipment().getBoots();
        }

        return (helmet != null ? helmet.getEnchantmentLevel(Enchantment.FIRE_PROTECTION) : 0) +
                (chestplate != null ? chestplate.getEnchantmentLevel(Enchantment.FIRE_PROTECTION) : 0) +
                (leggings != null ? leggings.getEnchantmentLevel(Enchantment.FIRE_PROTECTION) : 0) +
                (boots != null ? boots.getEnchantmentLevel(Enchantment.FIRE_PROTECTION) : 0);
    }

    public static void setLastTickDamage(Player player, double damage) {
        lastTickDamage.put(player, damage);
    }

    public static void thornsCheck(LivingEntity entity, LivingEntity attacker) {
        ItemStack helmet = null;
        ItemStack chestplate = null;
        ItemStack leggings = null;
        ItemStack boots = null;

        if (entity instanceof Player player) {
            helmet = player.getInventory().getHelmet();
            chestplate = player.getInventory().getChestplate();
            leggings = player.getInventory().getLeggings();
            boots = player.getInventory().getBoots();
        } else if (entity.getEquipment() != null) {
            helmet = entity.getEquipment().getHelmet();
            chestplate = entity.getEquipment().getChestplate();
            leggings = entity.getEquipment().getLeggings();
            boots = entity.getEquipment().getBoots();
        }

        ArrayList<Integer> thorns = new ArrayList<>();
        if (helmet != null && helmet.containsEnchantment(Enchantment.THORNS)) thorns.add(helmet.getEnchantmentLevel(Enchantment.THORNS));
        if (chestplate != null && chestplate.containsEnchantment(Enchantment.THORNS)) thorns.add(chestplate.getEnchantmentLevel(Enchantment.THORNS));
        if (leggings != null && leggings.containsEnchantment(Enchantment.THORNS)) thorns.add(leggings.getEnchantmentLevel(Enchantment.THORNS));
        if (boots != null && boots.containsEnchantment(Enchantment.THORNS)) thorns.add(boots.getEnchantmentLevel(Enchantment.THORNS));

        double damage = 0;

        for (int lvl : thorns) {
            double result = (Math.random() <= (0.15 * lvl)) ? ((Math.random() * 4) + 1) : 0;
            if (result > damage) damage = result;
        }

        if (damage == 0) return;

        DamageSource damageSource = DamageSource.builder(DamageType.THORNS).withCausingEntity(entity).withDirectEntity(entity).withDamageLocation(entity.getLocation()).build();
        attacker.damage(damage, damageSource);
    }
}
