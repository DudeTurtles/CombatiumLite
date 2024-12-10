package com.combatium.listeners.util;

import com.combatium.CombatiumLite;
import com.combatium.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;

import java.util.HashMap;
import java.util.Map;

public class AnimationListener implements Listener {
    private static final Map<Player, Integer> lastEntry = new HashMap<>();
    private static final Map<Player, Integer> validHit = new HashMap<>();

    private static boolean isSameTick(Player player) {
        if (validHit.containsKey(player) && validHit.get(player) == Scheduler.getCurrentTick()) {
            return false;
        }
        if (lastEntry.containsKey(player)) {
            return lastEntry.get(player) == Scheduler.getCurrentTick();
        }

        return false;
    }

    public static void cancelAnim(Player player) {
        lastEntry.put(player, Scheduler.getCurrentTick());
    }

    public static void validHit(Player player) {
        validHit.put(player, Scheduler.getCurrentTick());
    }

    @EventHandler
    public void onAnimation(PlayerAnimationEvent event) {
        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING && isSameTick(event.getPlayer()) && CombatiumLite.improvedAnimations) {
            event.setCancelled(true);
        }
    }
}
