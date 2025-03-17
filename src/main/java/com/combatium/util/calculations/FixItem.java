package com.combatium.util.calculations;

import com.combatium.CombatiumLite;
import com.combatium.util.active.ReachUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.combatium.listeners.snapshot.CombatifyListener.isCombatifyPlayer;
import static com.combatium.util.calculations.ItemDataHandler.checkItemDataDouble;


public class FixItem {
    public static final NamespacedKey ATTACK_SPEED = new NamespacedKey("combatium", "attack_speed");
    public static final NamespacedKey REACH = new NamespacedKey("combatium", "reach");
    public static final NamespacedKey ATTACK_DAMAGE = new NamespacedKey("combatium", "attack_damage");
    private static final java.lang.invoke.VarHandle API_ITEM_STACK_CRAFT_DELEGATE_FIELD;

    static {
        try {
            API_ITEM_STACK_CRAFT_DELEGATE_FIELD = java.lang.invoke.MethodHandles.privateLookupIn(
                    ItemStack.class,
                    java.lang.invoke.MethodHandles.lookup()
            ).findVarHandle(ItemStack.class, "craftDelegate", ItemStack.class);
        } catch (final IllegalAccessException | NoSuchFieldException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static ItemMeta fixSnowball(ItemStack item) {
        if (item.getItemMeta() == null) {
            return item.getItemMeta();
        }
        ItemMeta meta = item.getItemMeta();
        if (item.getType() == Material.SNOWBALL) {
            meta.setMaxStackSize(64);
            return meta;
        }
        return null;
    }

    public static ItemStack makeSwordBlock(ItemStack item) {
        if (!isSword(item.getType())) return null;
        Consumable consumable = Consumable.builder().
                consumeSeconds(99999).
                hasConsumeParticles(false).
                animation(ItemUseAnimation.BLOCK).
                build();
        net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.fromBukkitCopy(item);
        DataComponentPatch test = DataComponentPatch.builder().set(DataComponents.CONSUMABLE, consumable).build();
        nmsItem.applyComponents(test);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static ItemStack fixLiquidConsumeTime(ItemStack item) {
        if (!isLiquid(item.getType())) return null;
        net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.fromBukkitCopy(item);
        Consumable itemConsume = nmsItem.getComponents().get(DataComponents.CONSUMABLE);
        if (itemConsume == null) return null;
        if (itemConsume.consumeSeconds() == 1) return null;
        Consumable.Builder builder = Consumable.builder().
                consumeSeconds(1).
                hasConsumeParticles(itemConsume.hasConsumeParticles()).
                animation(itemConsume.animation()).
                sound(itemConsume.sound());
        for (ConsumeEffect effect : itemConsume.onConsumeEffects()) builder.onConsume(effect);
        DataComponentPatch test = DataComponentPatch.builder().set(DataComponents.CONSUMABLE, builder.build()).build();
        nmsItem.applyComponents(test);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static ItemMeta fixPotions(ItemStack item) {
        if (item.getItemMeta() == null || !(item.getType() == Material.POTION)) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta.hasMaxStackSize() && meta.getMaxStackSize() == 16) return null;
        meta.setMaxStackSize(16);
        return meta;
    }

    public static ItemMeta fixAttackSpeed(ItemStack item, Player player) {
        if (item == null || item.getItemMeta() == null || !ReachUtil.Tools.contains(item.getType())) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        boolean altered = false;
        double dataAttackSpeed = (CombatiumLite.supportExternalWeaponMods) ? checkItemDataDouble(ATTACK_SPEED, item, defaultAttackSpeed(item.getType())) : defaultAttackSpeed(item.getType());

        if (CombatiumLite.combatifySupport) {
            if (isCombatifyPlayer(player)) {
                if (meta.getAttributeModifiers() != null && meta.getAttributeModifiers().get(Attribute.ATTACK_SPEED).isEmpty()) {
                    meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED, dataAttackSpeed - 2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    altered = true;
                } else if (meta.getAttributeModifiers() != null) {
                    AttributeModifier modifier = meta.getAttributeModifiers().get(Attribute.ATTACK_SPEED).iterator().next();
                    if (modifier.getAmount() != dataAttackSpeed - 2.5) {
                        meta.removeAttributeModifier(Attribute.ATTACK_SPEED, modifier);
                        meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED, dataAttackSpeed - 2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                        altered = true;
                    }
                } else {
                    meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED, dataAttackSpeed - 2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    altered = true;
                }
            } else {
                int attackCooldown = (int) Math.round(20d / dataAttackSpeed);
                double attackSpeed = CombatiumLite.cooldownShowsFullCharge ? ((10d / attackCooldown) - 1.25) : (20d / attackCooldown) - 2.5;
                if (meta.getAttributeModifiers() != null && meta.getAttributeModifiers().get(Attribute.ATTACK_SPEED).isEmpty()) {
                    meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED, attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    altered = true;
                } else if (meta.getAttributeModifiers() != null) {
                    AttributeModifier modifier = meta.getAttributeModifiers().get(Attribute.ATTACK_SPEED).iterator().next();
                    if (modifier.getAmount() != attackSpeed) {
                        meta.removeAttributeModifier(Attribute.ATTACK_SPEED, modifier);
                        meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED, attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                        altered = true;
                    }
                } else {
                    meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED, attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    altered = true;
                }
            }
        } else {
            int attackCooldown = (int) Math.round(20d / dataAttackSpeed);
            if (meta.getAttributeModifiers(Attribute.ATTACK_SPEED) == null) {
                double attackSpeed = CombatiumLite.cooldownShowsFullCharge ? ((10d / attackCooldown) - 1.25) : (20d / attackCooldown) - 2.5;
                meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED, attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                altered = true;
            } else {
                AttributeModifier modifier = meta.getAttributeModifiers(Attribute.ATTACK_SPEED).iterator().next();
                double attackSpeed = CombatiumLite.cooldownShowsFullCharge ? ((10d / attackCooldown) - 1.25) : (20d / attackCooldown) - 2.5;
                if (modifier.getAmount() != attackSpeed) {
                    meta.removeAttributeModifier(Attribute.ATTACK_SPEED, modifier);
                    meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED, attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                    altered = true;
                }
            }
        }

        return altered ? meta : null;
    }

    public static ItemMeta fixReach(ItemStack item) {
        if (item == null || item.getItemMeta() == null || !ReachUtil.Tools.contains(item.getType())) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();

        if (CombatiumLite.supportExternalWeaponMods) {
            double dataReach = checkItemDataDouble(REACH, item, ReachUtil.getReach(item.getType()));
            if (meta.getAttributeModifiers(Attribute.ENTITY_INTERACTION_RANGE) == null) {
                meta.addAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE, new AttributeModifier(REACH, dataReach - 2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                return meta;
            } else {
                AttributeModifier modifier = meta.getAttributeModifiers(Attribute.ENTITY_INTERACTION_RANGE).iterator().next();
                if (modifier.getAmount() != dataReach - 2.5) {
                    meta.removeAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE, modifier);
                    meta.addAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE, new AttributeModifier(REACH, dataReach - 2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                    return meta;
                }
            }
        } else if (meta.getAttributeModifiers(Attribute.ENTITY_INTERACTION_RANGE) == null) {
            meta.addAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE, new AttributeModifier(REACH, ReachUtil.getReach(item.getType()) - 2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            return meta;
        }

        return null;
    }

    public static ItemMeta updateLore(ItemStack item) {
        //Checks if an items lore needs to be updated
        if (item == null || item.getItemMeta() == null || !ReachUtil.Tools.contains(item.getType())) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        double attackSpeed = (CombatiumLite.supportExternalWeaponMods) ? checkItemDataDouble(ATTACK_SPEED, item, defaultAttackSpeed(item.getType())) : defaultAttackSpeed(item.getType());
        double attackDamage = (CombatiumLite.supportExternalWeaponMods) ? checkItemDataDouble(ATTACK_DAMAGE, item, PlayerHitUtil.attackDamage(item, null)) : PlayerHitUtil.attackDamage(item, null);
        double reach = (CombatiumLite.supportExternalWeaponMods) ? checkItemDataDouble(REACH, item, ReachUtil.getReach(item.getType())) : ReachUtil.getReach(item.getType());

        //Checks if the lore is already updated
        if (meta.lore() != null && !Objects.requireNonNull(meta.lore()).isEmpty()) {
            return null;
        }

        @Nullable List<Component> lore = new ArrayList<>();
        lore.add(Component.text(" "));
        lore.add(Component.text("When in Main Hand:").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
        lore.add(Component.text(" ").append(Component.text(format(attackDamage)).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_GREEN).append(Component.text(" Attack Damage"))));
        lore.add(Component.text(" ").append(Component.text(format(attackSpeed)).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_GREEN).append(Component.text(" Attack Speed"))));
        if (reach != 2.5) lore.add(Component.text(" ").append(Component.text(format(reach)).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_GREEN).append(Component.text(" Attack Reach"))));
        meta.lore(lore);

        return meta;
    }

    private static String format(double value) {
        DecimalFormat df = new DecimalFormat("0.#");
        return df.format(value);
    }

    public static double defaultAttackSpeed(Material item) {
        return switch (item) {
            case MACE -> 1;
            case WOODEN_HOE, WOODEN_AXE, STONE_AXE, GOLDEN_AXE, IRON_AXE, DIAMOND_AXE, NETHERITE_AXE, WOODEN_SHOVEL,
                 STONE_SHOVEL, GOLDEN_SHOVEL, IRON_SHOVEL, DIAMOND_SHOVEL, NETHERITE_SHOVEL, TRIDENT -> 2;
            case WOODEN_SWORD, STONE_SWORD, GOLDEN_SWORD, IRON_SWORD, IRON_HOE, DIAMOND_SWORD, NETHERITE_SWORD -> 3;
            case GOLDEN_HOE, DIAMOND_HOE, NETHERITE_HOE -> 3.5;
            default -> 2.5;
        };
    }

    public static double attackSpeed(ItemStack item) {
        if (item == null) return 2.5;
        if (item.getItemMeta() == null) return defaultAttackSpeed(item.getType());

        return checkItemDataDouble(ATTACK_SPEED, item, defaultAttackSpeed(item.getType()));
    }

    public static boolean isSword(Material item) {
        return switch (item) {
            case WOODEN_SWORD, STONE_SWORD, GOLDEN_SWORD, IRON_SWORD, DIAMOND_SWORD, NETHERITE_SWORD -> true;
            default -> false;
        };
    }

    private static boolean isLiquid(Material item) {
        return switch (item) {
            case HONEY_BOTTLE, POTION, MILK_BUCKET, BEETROOT_SOUP, MUSHROOM_STEW, RABBIT_STEW, SUSPICIOUS_STEW-> true;
            default -> false;
        };
    }
}
