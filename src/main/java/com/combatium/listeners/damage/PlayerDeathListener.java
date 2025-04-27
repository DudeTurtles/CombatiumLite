package com.combatium.listeners.damage;

import com.combatium.CombatiumLite;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDeathListener implements Listener {
    private final Map<UUID, Integer> lastDeath = new HashMap<>();
    private final Map<UUID, Boolean> cancelled = new HashMap<>();

    /*
     * Ok, I know this code seems absolutely ridiculous, but you gotta trust me, it is absolutely necessary.
     * Paper / Spigot is dum as fuck, and does a death check on damage events which get cancelled. This leads to
     * player deaths getting called twice since it gets called for the actual damage, and the cancelled event.
     * Unfortunately, if I cancel either death, the player either doesn't die, or only dies client side.
     * This fix helps keep death messages normal, and, hopefully, prevents plugins from getting hit with the double deaths.
     * That being said, please put any death listeners at a priority between LOWEST and MONITOR to ensure compatibility.
     */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathStart(PlayerDeathEvent event) {
        if (lastDeath.containsKey(event.getPlayer().getUniqueId()) && Math.abs(CombatiumLite.getScheduler().getCurrentTick() - lastDeath.get(event.getEntity().getUniqueId())) <= 1) {
            cancelled.put(event.getPlayer().getUniqueId(), true);
            event.setCancelled(true);
            event.deathMessage(null);
        }
        lastDeath.put(event.getPlayer().getUniqueId(), CombatiumLite.getScheduler().getCurrentTick());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeathEnd(PlayerDeathEvent event) {
        if (cancelled.containsKey(event.getPlayer().getUniqueId()) && cancelled.get(event.getPlayer().getUniqueId())) {
            cancelled.remove(event.getPlayer().getUniqueId());
            event.setCancelled(false);
            event.deathMessage(null);
        }
    }
}
