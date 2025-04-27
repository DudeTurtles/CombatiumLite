package com.combatium.util.active;

import com.combatium.CombatiumLite;
import com.combatium.listeners.snapshot.CombatifyListener;
import com.combatium.util.ping.PingManager;
import net.minecraft.util.Mth;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.attribute.CraftAttribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.*;

public class ReachUtil {
    private final Map<Player, Integer> playerCooldowns = new HashMap<>();
    private final Map<Player, Integer> cooldownTime = new HashMap<>();
    private final List<Player> playersWhoMissed = new ArrayList<>();
    private final Map<Player, Integer> rateLimit = new HashMap<>();
    private int missTime = 4;
    private int rateLimitTicks= 3;
    private final NamespacedKey chargedReach = new NamespacedKey("combatify", "charged_reach");
    private final AttributeModifier chargedReachModifier = new AttributeModifier(chargedReach, 1, AttributeModifier.Operation.ADD_NUMBER);

    public void resetCooldown(Player player, boolean wasMiss) {
        rateLimit(player);

        playerCooldowns.put(player, CombatiumLite.getScheduler().getCurrentTick());
        cooldownTime.put(player, getCooldown(player) * 2);
        if (wasMiss) {
            if (!playersWhoMissed.contains(player)) playersWhoMissed.add(player);
        } else playersWhoMissed.remove(player);
    }

    public double getChargePercent(Player player, float baseTime) {
        if (!playerCooldowns.containsKey(player)) {
            resetCooldown(player, false);
            return 0;
        }
        return Mth.clamp(((CombatiumLite.getScheduler().getCurrentTick() - playerCooldowns.get(player)) + baseTime) / cooldownTime.get(player), 0.0F, 1.0F) * 2;
    }

    public boolean isOverCharged(Player player, float baseTime) {
        return getChargePercent(player, baseTime) > 1.95;
    }

    public boolean isCharged(Player player, float baseTime) {
        if (getChargePercent(player, baseTime) < 1.0) {
            if (!playerCooldowns.containsKey(player)) return false;
            return playersWhoMissed.contains(player) && (CombatiumLite.getScheduler().getCurrentTick() - playerCooldowns.get(player) + baseTime) > missTime;
        }

        return true;
    }

    public void updatePlayerReach(Player player) {
        if (CombatifyListener.isCombatifyPlayer(player)) {
            return;
        }

        //Makes it so players can hit immediately after shielding, but attack isn't fully charged
        if (player.isBlocking() && isCharged(player, 0.0F)) {
            playerCooldowns.put(player, CombatiumLite.getScheduler().getCurrentTick());
            cooldownTime.put(player, 0);
        }

        if (isOverCharged(player, (CombatiumLite.improvedChargedHits ? PingManager.getPingTicks(player) : 0.0F)) && !player.isSneaking()) {
            AttributeInstance attribute = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
            assert attribute != null;
            if (attribute.getModifier(chargedReach) == null) attribute.addModifier(chargedReachModifier);
        } else {
            Objects.requireNonNull(player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE)).removeModifier(chargedReach);
        }
    }

    public void removePlayer(Player player) {
        playerCooldowns.remove(player);
        cooldownTime.remove(player);
        playersWhoMissed.remove(player);
    }

    //Cooldown time in ticks (3.5 | 3 | 2.5 | 2 | 1)
    public int getCooldown(LivingEntity e) {
        var attrib = Objects.requireNonNull(e.getAttribute(Attribute.ATTACK_SPEED));
        double attackSpeed = attrib.getValue();
        double mod = !CombatiumLite.hasteFix ? 1.5 : calculateValueFromBase(1.5, attrib.getModifiers(), attrib.getAttribute());
        attackSpeed -= mod;
        if (attackSpeed <= 0)
            attackSpeed += mod;
        attackSpeed = Mth.clamp(attackSpeed, 0.1, 1024.0);
        attackSpeed = 1.0 / attackSpeed * 20.0 + 0.5;
        return (int) (attackSpeed);
    }
    public double calculateValueFromBase(double attributeInstanceBaseValue, Collection<AttributeModifier> modifiers, Attribute attribute) {
        List<AttributeModifier> multiplyBaseList = modifiers
                .stream()
                .filter(attributeModifier -> attributeModifier.getOperation() == AttributeModifier.Operation.ADD_SCALAR)
                .toList();
        List<AttributeModifier> multiplyTotalList = modifiers
                .stream()
                .filter(attributeModifier -> attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_SCALAR_1)
                .toList();

        double attributeInstanceFinalValue = attributeInstanceBaseValue;

        for(AttributeModifier attributeModifier2 : multiplyBaseList) {
            attributeInstanceFinalValue += attributeInstanceBaseValue * attributeModifier2.getAmount();
        }

        for(AttributeModifier attributeModifier2 : multiplyTotalList) {
            attributeInstanceFinalValue *= 1.0 + attributeModifier2.getAmount();
        }

        return ((CraftAttribute)attribute).getHandle().sanitizeValue(attributeInstanceFinalValue);
    }

    public double getReach(Material material) {
        return switch (material) {
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, DIAMOND_SWORD, NETHERITE_SWORD -> 3.0;
            case TRIDENT, WOODEN_HOE, STONE_HOE, IRON_HOE, GOLDEN_HOE, DIAMOND_HOE, NETHERITE_HOE -> 3.5;
            default -> 2.5;
        };
    }

    public double getReach(Player player) {
        return getReach(player.getInventory().getItemInMainHand().getType()) + (isOverCharged(player, 1.0F) ? 1 : 0);
    }

    public boolean isRateLimited(Player player) {
        if (!rateLimit.containsKey(player) || !CombatiumLite.rateLimit) return false;
        return rateLimit.get(player) >= CombatiumLite.getScheduler().getCurrentTick();
    }

    public void rateLimit(Player player) {
        rateLimit.put(player, CombatiumLite.getScheduler().getCurrentTick() + Math.min(rateLimitTicks, getCooldown(player)));
    }

    public void setRateLimitTicks(int ticks) {
        rateLimitTicks = ticks;
    }

    public void setMissTime(int time) {
        missTime = time;
    }
}
