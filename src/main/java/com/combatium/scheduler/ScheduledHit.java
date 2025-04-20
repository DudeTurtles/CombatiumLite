package com.combatium.scheduler;

import com.combatium.util.active.PlayerDamageEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import static com.combatium.util.active.ReachUtil.resetCooldown;
import static com.combatium.util.calculations.ValidHitChecks.isValidHit;

public class ScheduledHit {
    Player attacker;
    LivingEntity target;
    EntityDamageEvent.DamageCause damageCause;
    int scheduleTime;

    public ScheduledHit(Player attacker, LivingEntity target, EntityDamageEvent.DamageCause damageCause, int inTicks) {
        this.attacker = attacker;
        this.target = target;
        this.damageCause = damageCause;
        this.scheduleTime = Scheduler.getCurrentTick() + inTicks;
    }

    public int getScheduleTime() {
        return scheduleTime;
    }

    public void execute() {
        ItemStack weapon = attacker.getInventory().getItemInMainHand();

        if (!isValidHit(attacker, target)) {
            resetCooldown(attacker, true);
            return;
        }
        PlayerDamageEntity.playerDamageEntityEvent(attacker, target, weapon, damageCause, true);
        net.minecraft.world.entity.LivingEntity nmsAttacker = ((CraftLivingEntity) attacker).getHandle();
        nmsAttacker.swing(nmsAttacker.swingingArm, true);
    }
}
