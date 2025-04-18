package com.combatium.util.calculations;

import com.combatium.CombatiumLite;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.combatium.util.active.ReachUtil.getHaste;
import static com.combatium.util.active.ReachUtil.isOverCharged;
import static org.bukkit.potion.PotionEffectType.*;

public class PlayerHitUtil {
    private static Boolean backShots;
    private static final Map<Player, Double> lastTickDamage = new HashMap<>();

    public static float getRawDamage(Player attacker, Entity damagedEntity, ItemStack weapon, boolean isBlocked, double blockAmount, EntityDamageEvent.DamageCause damageCause) {
        float damage = (float) attackDamage(weapon, attacker);

        ItemMeta meta = weapon.getItemMeta();
        if (meta != null && meta.hasEnchants()) {
            if (meta.hasEnchant(Enchantment.SHARPNESS)) {
                damage += ((meta.getEnchantLevel(Enchantment.SHARPNESS) / 2.0f) + 0.5f);
            }
            if (meta.hasEnchant(Enchantment.IMPALING) && (damagedEntity.isInWaterOrRain())) {
                damage += (meta.getEnchantLevel(Enchantment.IMPALING) * 2.5f);
            }
            if (DisableShieldUtil.getCleavingLevel(weapon) > 0) {
                damage += 1 + DisableShieldUtil.getCleavingLevel(weapon);
            }
            if (meta.hasEnchant(Enchantment.BANE_OF_ARTHROPODS) && (damagedEntity instanceof Spider || damagedEntity instanceof Silverfish ||
                    damagedEntity instanceof Bee || damagedEntity instanceof Endermite)) {
                damage += (2.5f * meta.getEnchantLevel(Enchantment.BANE_OF_ARTHROPODS));
            }
            if (meta.hasEnchant(Enchantment.SMITE) && (damagedEntity instanceof Zombie || damagedEntity instanceof Skeleton ||
                    damagedEntity instanceof WitherSkeleton || damagedEntity instanceof Stray || damagedEntity instanceof Phantom ||
                    damagedEntity instanceof ZombieHorse || damagedEntity instanceof SkeletonHorse || damagedEntity instanceof Wither ||
                    damagedEntity instanceof Zoglin || damagedEntity instanceof ZombifiedPiglin)) {
                damage += (2.5f * meta.getEnchantLevel(Enchantment.SMITE));
            }
            if (meta.hasEnchant(Enchantment.SWEEPING_EDGE) && damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                float sweepLevel = meta.getEnchantLevel(Enchantment.SWEEPING_EDGE);
                return 1 + (damage * (sweepLevel / (2f * (sweepLevel + 1f))));
            } else if (damageCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                return 0;
            }
        }

        //Calculate damage from critical hits, strength, and weakness
        if (attacker.hasPotionEffect(STRENGTH)) {
            damage *= 1.2f + (0.2f * Objects.requireNonNull(attacker.getPotionEffect(STRENGTH)).getAmplifier());
        }
        if (attacker.hasPotionEffect(WEAKNESS)) {
            damage *= 0.8f - (0.2f * Objects.requireNonNull(attacker.getPotionEffect(WEAKNESS)).getAmplifier());
        }

        if (weapon.getType() == Material.MACE) {
            net.minecraft.world.item.ItemStack nmsWeapon = CraftItemStack.asNMSCopy(weapon);
            net.minecraft.world.entity.LivingEntity nmsEntity = ((CraftLivingEntity) damagedEntity).getHandle();
            net.minecraft.world.entity.LivingEntity attackerNMS = ((CraftLivingEntity) attacker).getHandle();
            damage += nmsWeapon.getItem().getAttackDamageBonus(nmsEntity, damage, attackerNMS.damageSources().playerAttack((net.minecraft.world.entity.player.Player) attackerNMS));
        }

        if (attacker.getFallDistance() > 0 && !attacker.isInsideVehicle() && !attacker.isOnGround() && !attacker.isFlying() && (damageCause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            damage *= (isOverCharged(attacker, weapon, getHaste(attacker)) || !CombatiumLite.weakerSpamCrits) ? 1.5f : 1.25f;
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
        if (meta.hasEnchant(Enchantment.IMPALING) && damagedEntity.isInWaterOrRain()) return true;
        if (meta.hasEnchant(Enchantment.BANE_OF_ARTHROPODS) && (damagedEntity instanceof Spider || damagedEntity instanceof Silverfish ||
                damagedEntity instanceof Bee || damagedEntity instanceof Endermite)) return true;
        return meta.hasEnchant(Enchantment.SMITE) && (damagedEntity instanceof Zombie || damagedEntity instanceof Skeleton || damagedEntity instanceof WitherSkeleton ||
                damagedEntity instanceof Stray || damagedEntity instanceof Phantom || damagedEntity instanceof ZombieHorse || damagedEntity instanceof SkeletonHorse ||
                damagedEntity instanceof Wither || damagedEntity instanceof Zoglin || damagedEntity instanceof ZombifiedPiglin);
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

    public static double attackDamage(ItemStack weapon, @Nullable Player player) {
        if (player != null && CombatiumLite.swapAccuracy && lastTickDamage.containsKey(player)) return lastTickDamage.get(player);

        return (CombatiumLite.supportExternalWeaponMods) ? ItemDataHandler.checkItemDataDouble(FixItem.ATTACK_DAMAGE, weapon, defaultAttackDamage(weapon.getType())) : defaultAttackDamage(weapon.getType());
    }

    private static double defaultAttackDamage(Material type) {
        return switch (type) {
            case IRON_HOE, IRON_SHOVEL, DIAMOND_HOE, WOODEN_PICKAXE, GOLDEN_PICKAXE -> 3;
            case WOODEN_SWORD, GOLDEN_SWORD, STONE_SWORD, DIAMOND_SHOVEL, NETHERITE_HOE -> 4;
            case IRON_SWORD, WOODEN_AXE, GOLDEN_AXE, STONE_AXE, NETHERITE_SHOVEL -> 5;
            case DIAMOND_SWORD, IRON_AXE, MACE -> 6;
            case NETHERITE_SWORD, DIAMOND_AXE, TRIDENT -> 7;
            case NETHERITE_AXE -> 8;
            default -> 2;
        };
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

    // Get the base damage given the final damage of a weapon
    public static float invertFinalDamage(float damage, LivingEntity entity, ItemStack weapon, boolean isErrorDamage) {
        int resistance = entity.getPotionEffect(RESISTANCE) == null ? 0 : Objects.requireNonNull(entity.getPotionEffect(RESISTANCE)).getAmplifier();

        float armor = (float) Objects.requireNonNull(entity.getAttribute(Attribute.ARMOR)).getValue();
        float toughness = (float) Objects.requireNonNull(entity.getAttribute(Attribute.ARMOR_TOUGHNESS)).getValue();

        float potionPassPercent = 1 - ( resistance * 0.2f);
        float protPassPercent = (float) getProtPassPercent(entity);

        float midPointDamage = ((armor * (toughness + 8)) / 5) * (1 - 0.15f * weapon.getEnchantmentLevel(Enchantment.BREACH));

        // Updated calculation to account for breach enchantment
        float armorBlockPercent = ((Math.max(((armor) / 5), (armor - ((4 * midPointDamage) / (toughness + 8))))) / 25);
        float armorPassPercent = (1 - (armorBlockPercent * (1 - 0.15f * weapon.getEnchantmentLevel(Enchantment.BREACH))));
        float armorPassthrough = (midPointDamage * armorPassPercent);

        float midPointFinal = (armorPassthrough * protPassPercent * potionPassPercent);

        /*
         * Error damage is the issue when dealing damage to a player, if you ignore shields, the damage is dealt as
         * Damage + Final Damage, where damage is the raw damage, and final damage is the damage after armor and resistance
         * are accounted for. This function calculates the input damage, needed to deal the desired final damage.
         */
        if (isErrorDamage) {
            if (damage < (midPointFinal + midPointDamage)) {
                // Damage is less than the midpoint, use the 4 * raw damage / (toughness + 8) formula
                float a = (4 * potionPassPercent * protPassPercent * (1 - 0.15f * weapon.getEnchantmentLevel(Enchantment.BREACH))) / (25 * (toughness + 8));
                float b = 1 + potionPassPercent * protPassPercent - ((potionPassPercent * protPassPercent * armor * (1 - 0.15f * weapon.getEnchantmentLevel(Enchantment.BREACH))) / 25);
                float c = -1 * damage;
                float sqrt = (float) Math.sqrt(b * b - 4f * a * c);

                return ((b + sqrt) / (2 * a) > damage) ? (sqrt - b) / (2 * a) : (b + sqrt) / (2 * a);
            } else {
                // Damage is greater than the midpoint, use the armor / 5 formula
                return damage / (protPassPercent * potionPassPercent * (1 - ((armor / 125) * (1 - 0.15f * weapon.getEnchantmentLevel(Enchantment.BREACH)))) + 1);
            }
        } else {
            if (damage < midPointFinal) {
                // Damage is less than the midpoint, use the 4 * raw damage / (toughness + 8) formula
                float a = (4f * (1 - 0.15f * weapon.getEnchantmentLevel(Enchantment.BREACH))) / (toughness + 8f);
                float b = 25 - (armor * (1 - 0.15f * weapon.getEnchantmentLevel(Enchantment.BREACH)));
                float c = -1 * ((25 * damage) / (protPassPercent * potionPassPercent));
                float sqrt = (float) Math.sqrt(b * b - 4 * a * c);

                return ((b + sqrt) / (2 * a) > damage) ? (sqrt - b) / (2 * a) : (b + sqrt) / (2 * a);
            } else {
                // Damage is greater than the midpoint, use the armor / 5 formula
                return damage / (protPassPercent * potionPassPercent * (1 - ((armor / 125) * (1 - 0.15f * weapon.getEnchantmentLevel(Enchantment.BREACH)))));
            }
        }
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
