package com.dudeturtles.combatium.listeners.util;

import com.dudeturtles.combatium.scheduler.Scheduler;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TickListener implements Listener {
    private final Scheduler scheduler;

    public TickListener(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @EventHandler
    public void onTick(ServerTickStartEvent tick) {
        scheduler.tick();
    }
}
