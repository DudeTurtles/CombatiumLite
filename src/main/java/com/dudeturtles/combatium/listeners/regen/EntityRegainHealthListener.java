package com.dudeturtles.combatium.listeners.regen;

import com.dudeturtles.combatium.CombatiumLite;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class EntityRegainHealthListener implements Listener {
    public boolean ignoreEventCall = false;
    public EntityRegainHealthListener() {
        CombatiumLite.getScheduler().registerEntityRegainHealthListener(this);
    }
    @EventHandler
    public void onHeal(EntityRegainHealthEvent event) {
        if (event.getRegainReason() == RegainReason.MAGIC) {
            event.setAmount(event.getAmount() * 1.5);
        } else if (event.getRegainReason() == RegainReason.SATIATED && !ignoreEventCall) { //Replaced base regeneration with custom regeneration ticks
            event.setCancelled(true);
        }
    }
}
