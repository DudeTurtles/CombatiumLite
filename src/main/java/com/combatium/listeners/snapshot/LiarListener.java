package com.combatium.listeners.snapshot;

import com.combatium.CombatiumLite;
import com.combatium.util.ItemPatcher;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.attribute.Attribute;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import net.minecraft.util.Mth;
import org.bukkit.entity.Player;

import java.util.*;

import static com.combatium.listeners.snapshot.CombatifyListener.isCombatifyPlayer;

public class LiarListener implements PacketListener {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!event.getPacketType().equals(PacketType.Play.Server.UPDATE_ATTRIBUTES)) return;

        WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(event);
        Player player = event.getPlayer();
        if (isCombatifyPlayer(player)) return; // Add some way to apply haste fix for combatify players (specific packet?)
        List<Integer> indexes = new ArrayList<>();
        Map<Integer, WrapperPlayServerUpdateAttributes.PropertyModifier> modifierMap = new HashMap<>();
        List<WrapperPlayServerUpdateAttributes.Property> properties = packet.getProperties();
        for (WrapperPlayServerUpdateAttributes.Property property : properties) {
            if (property.getAttribute() == Attributes.ATTACK_SPEED) {
                double speed = calculateValue(property.getValue(), property.getModifiers(), property.getAttribute());
                double mod = !CombatiumLite.hasteFix ? 1.5 : calculateValueFromBase(1.5, property.getModifiers(), property.getAttribute());
                double newSpeed = speed - mod;
                if (newSpeed <= 0) newSpeed += mod;
                newSpeed /= 2;
                for (; newSpeed > 0; newSpeed -= 0.0001) {
                    if (vanillaMath(newSpeed) == CTSMath(speed, mod) * 2) {
                        modifierMap.put(properties.indexOf(property), new WrapperPlayServerUpdateAttributes.PropertyModifier(ResourceLocation.minecraft(ItemPatcher.BASE_ATTACK_SPEED_CTS_ID.getPath()), newSpeed - 2.5, WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION));
                        break;
                    }
                }
                indexes.add(properties.indexOf(property));
            }
        }
        if (!indexes.isEmpty())
            for (Integer index : indexes) {
                WrapperPlayServerUpdateAttributes.PropertyModifier modifierToSend = modifierMap.get(index);
                List<WrapperPlayServerUpdateAttributes.PropertyModifier> newModifiers = new ArrayList<>(Collections.singletonList(modifierToSend));
                WrapperPlayServerUpdateAttributes.Property property = properties.remove(index.intValue());
                double mod = !CombatiumLite.hasteFix ? 1.5 : calculateValueFromBase(1.5, property.getModifiers(), property.getAttribute());
                properties.add(index, new WrapperPlayServerUpdateAttributes.Property(property.getAttribute(), property.getValue() - mod, newModifiers));
            }
        packet.setProperties(properties);
    }
    public static double calculateValue(double baseValue, Collection<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers, Attribute attribute) {
        double attributeInstanceBaseValue = baseValue;
        List<WrapperPlayServerUpdateAttributes.PropertyModifier> additionList = modifiers
                .stream()
                .filter(attributeModifier -> attributeModifier.getOperation() == WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION)
                .toList();

        for(WrapperPlayServerUpdateAttributes.PropertyModifier attributeModifier : additionList) {
            attributeInstanceBaseValue += attributeModifier.getAmount();
        }

        return calculateValueFromBase(attributeInstanceBaseValue, modifiers, attribute);
    }
    public static double calculateValueFromBase(double attributeInstanceBaseValue, Collection<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers, Attribute attribute) {
        List<WrapperPlayServerUpdateAttributes.PropertyModifier> multiplyBaseList = modifiers
                .stream()
                .filter(attributeModifier -> attributeModifier.getOperation() == WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_BASE)
                .toList();
        List<WrapperPlayServerUpdateAttributes.PropertyModifier> multiplyTotalList = modifiers
                .stream()
                .filter(attributeModifier -> attributeModifier.getOperation() == WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_TOTAL)
                .toList();

        double attributeInstanceFinalValue = attributeInstanceBaseValue;

        for(WrapperPlayServerUpdateAttributes.PropertyModifier attributeModifier2 : multiplyBaseList) {
            attributeInstanceFinalValue += attributeInstanceBaseValue * attributeModifier2.getAmount();
        }

        for(WrapperPlayServerUpdateAttributes.PropertyModifier attributeModifier2 : multiplyTotalList) {
            attributeInstanceFinalValue *= 1.0 + attributeModifier2.getAmount();
        }

        return attribute.sanitizeValue(attributeInstanceFinalValue);
    }
    private static int CTSMath(double attackSpeed, double mod) {
        double finalSpeed = attackSpeed - mod;
        if (finalSpeed <= 0)
            finalSpeed += mod;
        finalSpeed = Mth.clamp(finalSpeed, 0.1, 1024.0);
        finalSpeed = 1.0 / finalSpeed * 20.0 + 0.5;
        return (int) (finalSpeed);
    }
    private static int vanillaMath(double attackSpeed) {
        return (int) (1.0 / attackSpeed * 20.0);
    }
}
