package com.combatium.util.active;

import com.combatium.CombatiumLite;
import com.combatium.scheduler.Scheduler;
import com.combatium.util.ping.PingManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

import static com.combatium.listeners.snapshot.CombatifyListener.isCombatifyPlayer;
import static com.combatium.util.calculations.FixItem.attackSpeed;

public class ReachUtil {
    public static final Set<Material> Tools = Collections.unmodifiableSet(EnumSet.of(Material.TRIDENT,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.GOLDEN_AXE, Material.IRON_AXE,
            Material.DIAMOND_AXE, Material.NETHERITE_AXE, Material.WOODEN_SWORD, Material.STONE_SWORD,
            Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.WOODEN_HOE,
            Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE, Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL, Material.GOLDEN_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE, Material.MACE));

    private static final Map<Player, Integer> playerCooldowns = new HashMap<>();
    private static final Map<Player, Integer> cooldownTime = new HashMap<>();
    private static final Map<Player, Integer> rateLimit = new HashMap<>();
    private static int missTime = 4;
    private static int rateLimitTicks= 3;
    private static final NamespacedKey chargedReach = new NamespacedKey("combatium", "charged-reach");
    private static final NamespacedKey combatifyReach = new NamespacedKey("combatify", "charged_reach");
    private static final AttributeModifier chargedReachModifier = new AttributeModifier(chargedReach, 1, AttributeModifier.Operation.ADD_NUMBER);

    public static void resetCooldown(Player player, boolean wasMiss, ItemStack item) {
        rateLimit(player);

        if (wasMiss) {
            playerCooldowns.put(player, Scheduler.getCurrentTick());
            cooldownTime.put(player, missTime);
        }
        else {
            playerCooldowns.put(player, Scheduler.getCurrentTick());
            cooldownTime.put(player, getCooldown(item, getHaste(player)));
        }
    }

    public static int earlyHit(Player player) {
        int ticksRemaining = cooldownTime.get(player) - (Scheduler.getCurrentTick() - playerCooldowns.get(player));
        if (((double) ticksRemaining) / ((double) cooldownTime.get(player)) <= 0.2) return ticksRemaining + 1;
        else return 0;
    }

    public static boolean isOverCharged(Player player, ItemStack item, int haste) {
        int cooldown = cooldownTime.get(player);

        if (!playerCooldowns.containsKey(player)) {
            resetCooldown(player, false, item);
            return false;
        }

        return playerCooldowns.get(player) + (cooldown + getCooldown(item, haste)) <= Scheduler.getCurrentTick();
    }

    private static boolean checkOvercharge(Player player, ItemStack item, int haste) {
        if (!cooldownTime.containsKey(player) || !playerCooldowns.containsKey(player)) {
            resetCooldown(player, false, item);
            return false;
        }

        int cooldown = cooldownTime.get(player);

        if (CombatiumLite.improvedChargedHits) cooldown -= PingManager.getPingTicks(player);

        return playerCooldowns.get(player) + (cooldown + getCooldown(item, haste)) <= Scheduler.getCurrentTick();
    }

    public static boolean isCharged(Player player) {
        if (!playerCooldowns.containsKey(player)) {
            playerCooldowns.put(player, Scheduler.getCurrentTick());
            cooldownTime.put(player, 0);
            return false;
        }

        return Scheduler.getCurrentTick() >= playerCooldowns.get(player) + cooldownTime.get(player);
    }

    public static void updatePlayerReach(Player player) {
        if (isCombatifyPlayer(player)) {
            return;
        }

        //Makes it so players can hit immediately after shielding, but attack isn't fully charged
        if (player.isBlocking() && isCharged(player)) {
            playerCooldowns.put(player, Scheduler.getCurrentTick());
            cooldownTime.put(player, 0);
        }

        int haste = getHaste(player);


        if (checkOvercharge(player, player.getInventory().getItemInMainHand(), haste) && !player.isSneaking()) {
            AttributeInstance attribute = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
            if (attribute.getModifier(chargedReach) == null) attribute.addModifier(chargedReachModifier);
        } else {
            Objects.requireNonNull(player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE)).removeModifier(chargedReach);
        }
    }

    public static void removePlayer(Player player) {
        playerCooldowns.remove(player);
        cooldownTime.remove(player);
    }

    //Cooldown time in ticks (3.5 | 3 | 2.5 | 2 | 1)
    public static int getCooldown(ItemStack item, int haste) {
        double attackSpeed = attackSpeed(item);
        double hasteSpeed = (attackSpeed + 1.5) * 0.1 * haste;
        return (int) Math.round(20 / (attackSpeed + hasteSpeed));
    }

    public static double getReach(Material material) {
        return switch (material) {
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, DIAMOND_SWORD, NETHERITE_SWORD -> 3.0;
            case TRIDENT, WOODEN_HOE, STONE_HOE, IRON_HOE, GOLDEN_HOE, DIAMOND_HOE, NETHERITE_HOE -> 3.5;
            default -> 2.5;
        };
    }

    public static double getReach(Player player) {
        return getReach(player.getInventory().getItemInMainHand().getType()) + (isOverCharged(player, player.getInventory().getItemInMainHand(), getHaste(player)) ? 1 : 0);
    }

    public static int getHaste(Player player) {
        if (player.hasPotionEffect(PotionEffectType.HASTE)) {
            return Objects.requireNonNull(player.getPotionEffect(PotionEffectType.HASTE)).getAmplifier();
        }
        return 0;
    }

    public static boolean isRateLimited(Player player) {
        if (!rateLimit.containsKey(player) || !CombatiumLite.rateLimit) return false;
        return rateLimit.get(player) >= Scheduler.getCurrentTick();
    }

    public static void rateLimit(Player player) {
        rateLimit.put(player, Scheduler.getCurrentTick() + rateLimitTicks);
    }

    public static void setRateLimitTicks(int ticks) {
        rateLimitTicks = ticks;
    }

    public static void setMissTime(int time) {
        missTime = time;
    }
}
