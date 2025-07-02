package com.dudeturtles.combatium.scheduler;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.util.active.SweepingEdgeUtil;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Player;

import static com.dudeturtles.combatium.listeners.util.sweepingEdgeListeners.PlayerInteractListener.isPotentialFalseInput;

public record ScheduledSweep(Player player) {

    public void execute() {
        if (isPotentialFalseInput(player, 1)) return;
        SweepingEdgeUtil.Sweep(player, null);
        CombatiumLite.getReachUtil().resetCooldown(player, true);
        net.minecraft.world.entity.LivingEntity nmsAttacker = ((CraftLivingEntity) player).getHandle();
        nmsAttacker.swing(nmsAttacker.swingingArm, true);
    }
}
