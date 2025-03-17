package com.combatium.scheduler;

import com.combatium.util.active.SweepingEdgeUtil;
import org.bukkit.entity.Player;

import static com.combatium.listeners.util.sweepingEdgeListeners.PlayerInteractListener.isPotentialFalseInput;

public class ScheduledSweep {
    private final Player player;

    public ScheduledSweep(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void execute() {
        if (isPotentialFalseInput(player, 1)) return;

        SweepingEdgeUtil.Sweep(player, null);
    }
}
