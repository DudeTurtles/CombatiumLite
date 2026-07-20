package com.dudeturtles.combatium.listeners.regen;

import com.dudeturtles.combatium.CombatiumLite;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ExhaustListener implements Listener {
    @EventHandler
    public void onExhaust(EntityExhaustionEvent event) {
        if (event.getExhaustionReason() == EntityExhaustionEvent.ExhaustionReason.REGEN) event.setCancelled(true);
        if (event.getExhaustionReason() == EntityExhaustionEvent.ExhaustionReason.HUNGER_EFFECT && CombatiumLite.ctsHungerBuff) {
            PotionEffect potionEffect = event.getEntity().getPotionEffect(PotionEffectType.HUNGER);
            if (potionEffect == null) return;
            int amplifier = potionEffect.getAmplifier();
            event.setExhaustion(0.005F * (amplifier * amplifier * 5 + 1));
        }
    }
}
