package com.dudeturtles.combatium.scheduler;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.listeners.projectile.ProjectilePhaseListener;
import com.dudeturtles.combatium.listeners.regen.EntityRegainHealthListener;
import com.dudeturtles.combatium.util.active.PlayerDamageEntity;
import com.dudeturtles.combatium.util.active.ReachUtil;
import com.dudeturtles.combatium.util.active.TridentTracker;
import com.dudeturtles.combatium.util.calculations.PlayerHitUtil;
import com.dudeturtles.combatium.util.hitThroughTransparent.HitManager;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;

public class Scheduler {
    private final Plugin plugin;
    private int currentTick;
    private int regenTick;
    //The amount of ticks between each regen tick (Default 39)
    private final int regenTickDelay;
    private final ArrayList<ScheduledSweep> scheduledSweeps;
    private final ArrayList<ScheduledHit> scheduledHits;
    private final ArrayList<ScheduledHit> toRemove;
    private InvulnerabilityScheduler invulnerabilityScheduler;
    private ProjectilePhaseListener projectilePhaseListener;
    private EntityRegainHealthListener entityRegainHealthListener = null;
    private boolean disabledAlready;

    public Scheduler(Plugin plugin, int regenTickDelay) {
        this.plugin = plugin;
        this.regenTickDelay = regenTickDelay;
        currentTick = 0;
        regenTick = 0;
        scheduledSweeps = new ArrayList<>();
        scheduledHits = new ArrayList<>();
        toRemove = new ArrayList<>();
    }

    public void registerProjectilePhaseListener(ProjectilePhaseListener listener) {
        this.projectilePhaseListener = listener;
    }

    public void registerEntityRegainHealthListener(EntityRegainHealthListener listener) {
        this.entityRegainHealthListener = listener;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void scheduleHit(ScheduledHit hit) {
        scheduledHits.add(hit);
    }

    public void scheduleSweep(Player player) {
        scheduledSweeps.add(new ScheduledSweep(player));
    }

    public void tick() {
        if (!disabledAlready && CombatiumLite.antiProjectileGhosting) {
            Plugin consistentArrows = Bukkit.getPluginManager().getPlugin("ConsistentArrows");
            if (consistentArrows != null) Bukkit.getPluginManager().disablePlugin(consistentArrows);
            disabledAlready = true;
        }
        //Update the reach of each player
        ReachUtil reachUtil = CombatiumLite.getReachUtil();
        for (Player player : Bukkit.getOnlinePlayers()) {
            reachUtil.updatePlayerReach(player);
        }

        //Runs any scheduled Sweeping Edge hits
        for (ScheduledSweep sweep : scheduledSweeps) {
            sweep.execute();
        }
        scheduledSweeps.clear();

        //Checks if any scheduled hits should be run
        for (ScheduledHit hit : scheduledHits) {
            if (reachUtil.isCharged(hit.attacker, -1)) {
                hit.execute();
                toRemove.add(hit);
            }
        }

        for (ScheduledHit hit : toRemove) {
            scheduledHits.remove(hit);
        }
        toRemove.clear();

        //Regen health
        if (++regenTick == regenTickDelay) {
            regenTick = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                net.minecraft.world.entity.player.Player nmsPlayer = ((org.bukkit.craftbukkit.entity.CraftPlayer) player).getHandle();
                if ((Boolean.FALSE.equals(player.getWorld().getGameRuleValue(GameRules.NATURAL_HEALTH_REGENERATION)) || !nmsPlayer.isHurt() || player.getFoodLevel() <= 7)) continue;

                if (entityRegainHealthListener != null) entityRegainHealthListener.ignoreEventCall = true;
                boolean didHeal = heal(nmsPlayer, 1.0F, EntityRegainHealthEvent.RegainReason.SATIATED);
                if (entityRegainHealthListener != null) entityRegainHealthListener.ignoreEventCall = false;

                if (!nmsPlayer.getRandom().nextBoolean() || !didHeal) continue;
                FoodLevelChangeEvent event = new FoodLevelChangeEvent(player, player.getFoodLevel() - 1, null);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) player.setFoodLevel(Math.max(0, player.getFoodLevel() - 1));
            }
        }

        if (CombatiumLite.swapAccuracy) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerHitUtil.setLastTickDamage(player, player.getAttribute(Attribute.ATTACK_DAMAGE).getValue());
                PlayerDamageEntity.setLastTickWeapon(player, player.getInventory().getItemInMainHand());
            }
        }

        if (CombatiumLite.tridentsReturnFromVoid) {
            TridentTracker.checkTridents();
        }

        if (CombatiumLite.hitThroughTransparent) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                HitManager.updatePlayerData(player);
            }
        }

        if (projectilePhaseListener != null) {
            ProjectilePhaseListener.tick();
        }

        currentTick++;
    }

    public void register(InvulnerabilityScheduler invulnerabilityScheduler) {
        this.invulnerabilityScheduler = invulnerabilityScheduler;
    }

    public InvulnerabilityScheduler getInvulnerabilityScheduler() {
        return invulnerabilityScheduler;
    }

    public boolean heal(net.minecraft.world.entity.player.Player nmsPlayer, float amount, EntityRegainHealthEvent.RegainReason regainReason) {
        float health = nmsPlayer.getHealth();
        if (health > 0.0F) {
            EntityRegainHealthEvent event = new EntityRegainHealthEvent(nmsPlayer.getBukkitEntity(), amount, regainReason);
            if (nmsPlayer.valid) nmsPlayer.level().getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) return false;
            nmsPlayer.setHealth((float) (nmsPlayer.getHealth() + event.getAmount()));
        }
        return true;
    }
}
