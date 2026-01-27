package com.dudeturtles.combatium.util.active;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.listeners.snapshot.CombatifyListener;
import com.dudeturtles.combatium.util.ItemPatcher;
import com.dudeturtles.combatium.util.ping.PingManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.attribute.CraftAttribute;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.*;

public class ReachUtil {
    private final Map<Player, Integer> playerCooldowns = new HashMap<>();
    private final Map<Player, Integer> cooldownTime = new HashMap<>();
    private final List<Player> playersWhoMissed = new ArrayList<>();
    private final Map<Player, Integer> rateLimit = new HashMap<>();
    private int missTime = 4;
    private int rateLimitTicks = 4;
    public final NamespacedKey bukkitChargedReach = new NamespacedKey("combatify", "charged_reach");
    public final Identifier chargedReach = Identifier.fromNamespaceAndPath("combatify", "charged_reach");

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
        ItemStack weapon = ((CraftPlayer) player).getHandle().getActiveItem();
        float minAttackCharge = weapon.getOrDefault(DataComponents.MINIMUM_ATTACK_CHARGE, 0.5F) * 2;
        float strengthScale = (float) getChargePercent(player, baseTime);
        if (minAttackCharge > 0.0F && strengthScale < minAttackCharge) {
            if (weapon.has(DataComponents.MINIMUM_ATTACK_CHARGE) || !playerCooldowns.containsKey(player)) return false; // Don't allow weapons with custom minimum attack charge to use missed attack recovery
            return playersWhoMissed.contains(player) &&
                    (CombatiumLite.getScheduler().getCurrentTick() - playerCooldowns.get(player) + baseTime) > ((CombatifyListener.isCombatifyPlayer(player)) ? missTime - 1 : missTime);
        }

        return true;
    }

    public void updatePlayerReach(Player player) {
        ServerPlayer nms = ((CraftPlayer)player).getHandle();
        net.minecraft.world.entity.ai.attributes.AttributeInstance blockIntRange = nms.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (blockIntRange != null) {
            blockIntRange.removeModifier(Identifier.withDefaultNamespace("creative_mode_block_range"));
        }

        net.minecraft.world.entity.ai.attributes.AttributeInstance entityIntRange = nms.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (entityIntRange != null) {
            entityIntRange.removeModifier(Identifier.withDefaultNamespace("creative_mode_entity_range"));
        }
        if (CombatifyListener.isCombatifyPlayer(player)) {
            return;
        }

        //Makes it so players can hit immediately after shielding, but attack isn't fully charged
        if (player.isBlocking() && isCharged(player, 0.0F)) {
            playerCooldowns.put(player, CombatiumLite.getScheduler().getCurrentTick());
            cooldownTime.put(player, 0);
        }

        double chargedReachBonus = getChargedReach(player);
        net.minecraft.world.entity.ai.attributes.AttributeModifier modifier = new net.minecraft.world.entity.ai.attributes.AttributeModifier(chargedReach, chargedReachBonus, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
        if (isOverCharged(player, (CombatiumLite.improvedChargedHits ? PingManager.getPingTicks(player) * 2 : 0.0F)) && !((CraftPlayer) player).getHandle().isCrouching()) {
            entityIntRange.addOrUpdateTransientModifier(modifier);
        } else {
            entityIntRange.removeModifier(modifier);
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

    public double getChargedReach(Player bukkit) {
        net.minecraft.world.entity.player.Player nmsPlayer = ((CraftPlayer) bukkit).getHandle();
        CustomData data = nmsPlayer.getActiveItem().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!data.isEmpty())
            return data.copyTag().getFloatOr(ItemPatcher.CHARGED_REACH_KEY, 1.0F);
        return 1.0F;
    }

    public boolean isRateLimited(Player player) {
        if (!rateLimit.containsKey(player)) return false;
        return rateLimit.get(player) >= CombatiumLite.getScheduler().getCurrentTick();
    }

    public void rateLimit(Player player) {
        if (CombatifyListener.isCombatifyPlayer(player)) rateLimit.put(player, CombatiumLite.getScheduler().getCurrentTick() + Math.min(rateLimitTicks - 1, getCooldown(player) - 1));
        else rateLimit.put(player, CombatiumLite.getScheduler().getCurrentTick() + Math.min(rateLimitTicks, getCooldown(player)));
    }

    public void setRateLimitTicks(int ticks) {
        rateLimitTicks = ticks;
    }

    public void setMissTime(int time) {
        missTime = time;
    }
}
