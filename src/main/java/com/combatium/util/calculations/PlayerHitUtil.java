package com.combatium.util.calculations;

import com.combatium.CombatiumLite;
import com.combatium.util.active.ReachUtil;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.entity.CraftEntityType;
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

import static org.bukkit.potion.PotionEffectType.*;

public class PlayerHitUtil {
    private static Boolean backShots;
    private static final Map<Player, Double> lastTickDamage = new HashMap<>();
    private static final NamespacedKey enchantedDamageBonus = new NamespacedKey("combatium", "magic_damage_bonus");

    public static float getRawDamage(Player attacker, Entity damagedEntity, ItemStack weapon, boolean isBlocked, double blockAmount, EntityDamageEvent.DamageCause damageCause) {
        float damage = (float) attackDamage(attacker);
        float enchantedDamage = 0;

        ItemMeta meta = weapon.getItemMeta();
        if (meta != null && meta.hasEnchants()) {
            // TODO: Don't hardcode this
            if (meta.hasEnchant(Enchantment.SHARPNESS)) {
                enchantedDamage += ((meta.getEnchantLevel(Enchantment.SHARPNESS) / 2.0f) + 0.5f);
            }
            if (meta.hasEnchant(Enchantment.IMPALING) && (damagedEntity.isInWaterOrRain() || isEntityInTag(damagedEntity, EntityTypeTags.SENSITIVE_TO_IMPALING))) {
                enchantedDamage += (meta.getEnchantLevel(Enchantment.IMPALING) * 2.5f);
            }
            if (DisableShieldUtil.getCleavingLevel(weapon) > 0) {
                enchantedDamage += 1 + DisableShieldUtil.getCleavingLevel(weapon);
            }
            if (meta.hasEnchant(Enchantment.BANE_OF_ARTHROPODS) && isEntityInTag(damagedEntity, EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
                enchantedDamage += (2.5f * meta.getEnchantLevel(Enchantment.BANE_OF_ARTHROPODS));
            }
            if (meta.hasEnchant(Enchantment.SMITE) && isEntityInTag(damagedEntity, EntityTypeTags.SENSITIVE_TO_SMITE)) {
                enchantedDamage += (2.5f * meta.getEnchantLevel(Enchantment.SMITE));
            }
            if (enchantedDamage > 0) {
                damage = (float) enchantedDamage(attacker, enchantedDamage);
            }
            if (meta.hasEnchant(Enchantment.SWEEPING_EDGE) && damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                float sweepLevel = meta.getEnchantLevel(Enchantment.SWEEPING_EDGE);
                return 1 + (damage * (sweepLevel / (2f * (sweepLevel + 1f))));
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
            damage *= (ReachUtil.isOverCharged(attacker, 0.0F) || !CombatiumLite.weakerSpamCrits) ? 1.5f : 1.25f;
        }
        if (backShots && wasBackShots(attacker, damagedEntity)) {
            damage *= 1.5f;
        }

        else if (isBlocked) {
            if (damage > blockAmount) {
                return (float) (damage - blockAmount);
            } else {
                damagedEntity.getWorld().playSound(damagedEntity.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
                return 0;
            }
        }

        return damage;
    }

    public static boolean isEntityInTag(Entity entity, TagKey<EntityType<?>> tag) {
        return CraftEntityType.bukkitToMinecraft(entity.getType()).is(tag);
    }

    //Calculate the damage values after armor and resistance are accounted for
    public static float getFinalDamage(LivingEntity damagedEntity, float rawDamage, ItemStack weapon) {
        double protPassPercent = getProtPassPercent(damagedEntity);

        //Get the amount of damage dealt after armor is accounted for
        double armor = Objects.requireNonNull(damagedEntity.getAttribute(Attribute.ARMOR)).getValue();
        double toughness = Objects.requireNonNull(damagedEntity.getAttribute(Attribute.ARMOR_TOUGHNESS)).getValue();

        float armorBlockPercent = (float) ((Math.max(((armor) / 5), (armor - ((4 * rawDamage) / (toughness + 8))))) / 25);
        float armorPassPercent = (float) (1 - (armorBlockPercent * (1 - 0.15 * weapon.getEnchantmentLevel(Enchantment.BREACH))));

        //Get the percentage of damage dealt after resistance
        int resistance = damagedEntity.getPotionEffect(RESISTANCE) == null ? 0 : Objects.requireNonNull(damagedEntity.getPotionEffect(RESISTANCE)).getAmplifier();
        double potionPassPercent = 1 - ( resistance * 0.2);

        return (float) (rawDamage * armorPassPercent * protPassPercent * potionPassPercent);
    }

    public static boolean dealsEnchantDamage(ItemStack weapon, Entity damagedEntity) {
        if (!(weapon.getItemMeta() != null && weapon.getItemMeta().hasEnchants())) return false;
        ItemMeta meta = weapon.getItemMeta();
        if (meta.hasEnchant(Enchantment.SHARPNESS)) return true;
        if (meta.hasEnchant(Enchantment.IMPALING) && (damagedEntity.isInWaterOrRain() || isEntityInTag(damagedEntity, EntityTypeTags.SENSITIVE_TO_IMPALING))) return true;
        if (meta.hasEnchant(Enchantment.BANE_OF_ARTHROPODS) && isEntityInTag(damagedEntity, EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) return true;
        return meta.hasEnchant(Enchantment.SMITE) && isEntityInTag(damagedEntity, EntityTypeTags.SENSITIVE_TO_SMITE);
    }

    public static boolean wasBackShots(Player attacker, Entity damagedEntity) {
        double angle = getAngle(attacker, damagedEntity);
        return angle >= Math.toRadians(100);
    }

    public static double getAngle(Player attacker, Entity damagedEntity) {
        Vec3 vector = CraftLocation.toVec3D(attacker.getLocation());
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
                CombatiumLite.logger.log(Level.SEVERE, "Failed to retrieve riptide damage!" + e, e);
            }
        }
        if (CombatiumLite.swapAccuracy && lastTickDamage.containsKey(attacker)) return lastTickDamage.get(attacker);

        return Objects.requireNonNull(attacker.getAttribute(Attribute.ATTACK_DAMAGE)).getValue();
    }

    private static double enchantedDamage(Player attacker, float enchantedDamage) {
        if (attacker.isRiptiding()) return enchantedDamage + attackDamage(attacker);
        var attrib = Objects.requireNonNull(attacker.getAttribute(Attribute.ATTACK_DAMAGE));
        attrib.addTransientModifier(new AttributeModifier(enchantedDamageBonus, enchantedDamage, AttributeModifier.Operation.ADD_NUMBER));
        double res = attrib.getValue();
        attrib.removeModifier(enchantedDamageBonus);
        return res;
    }

    public static void setBackShots(Boolean backShots) {
        PlayerHitUtil.backShots = backShots;
    }

    private static double getProtPassPercent(LivingEntity entity) {
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

        //Get the percentage of damage dealt after protection
        int prot = (helmet != null ? helmet.getEnchantmentLevel(Enchantment.PROTECTION) : 0) +
                (chestplate != null ? chestplate.getEnchantmentLevel(Enchantment.PROTECTION) : 0) +
                (leggings != null ? leggings.getEnchantmentLevel(Enchantment.PROTECTION) : 0) +
                (boots != null ? boots.getEnchantmentLevel(Enchantment.PROTECTION) : 0);

        return 1 - (prot * 0.04f);
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

        DamageSource damageSource = DamageSource.builder(DamageType.THORNS).withCausingEntity(entity).withDirectEntity(entity).withDamageLocation(entity.getLocation()).build();
        attacker.damage(damage, damageSource);
    }
}
