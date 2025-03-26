package com.combatium.scheduler;

import com.combatium.CombatiumLite;
import com.combatium.util.active.PlayerDamageEntity;
import com.combatium.util.active.ReachUtil;
import com.combatium.util.active.TridentTracker;
import com.combatium.util.calculations.PlayerHitUtil;
import com.combatium.util.hitThroughTransparent.HitManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static com.combatium.util.calculations.FixItem.isSword;

public class Scheduler {
    static Plugin plugin;
    static int currentTick;
    static int regenTick;
    //The amount of ticks between each regen tick (Default 39)
    static int regenTickDelay;
    static ArrayList<ScheduledSweep> scheduledSweeps;
    static ArrayList<ScheduledHit> scheduledHits;
    static ArrayList<ScheduledHit> toRemove;
    Random random = new Random();

    public Scheduler(Plugin plugin, int regenTickDelay) {
        Scheduler.plugin = plugin;
        Scheduler.regenTickDelay = regenTickDelay;
        currentTick = 0;
        regenTick = 0;
        scheduledSweeps = new ArrayList<>();
        scheduledHits = new ArrayList<>();
        toRemove = new ArrayList<>();
    }

    public static int getCurrentTick() {
        return currentTick;
    }

    public static void scheduleHit(ScheduledHit hit) {
        scheduledHits.add(hit);
    }

    public static void scheduleSweep(Player player) {
        scheduledSweeps.add(new ScheduledSweep(player));
    }

    public void tick() {
        //Update the reach of each player
        for (Player player : Bukkit.getOnlinePlayers()) {
            ReachUtil.updatePlayerReach(player);
        }

        //Runs any scheduled Sweeping Edge hits
        for (ScheduledSweep sweep : scheduledSweeps) {
            sweep.execute();
        }
        scheduledSweeps.clear();

        //Checks if any scheduled hits should be run
        for (ScheduledHit hit : scheduledHits) {
            if (currentTick == hit.getScheduleTime()) {
                hit.execute();
                toRemove.add(hit);
            }
        }

        for (ScheduledHit hit : toRemove) {
            scheduledHits.remove(hit);
        }
        toRemove.clear();

        //Regen health
        if (regenTick == regenTickDelay) {
            regenTick = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (Boolean.FALSE.equals(player.getWorld().getGameRuleValue(GameRule.NATURAL_REGENERATION))) continue;
                if (player.getHealth() < Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue() &&
                        player.getFoodLevel() > 7 && player.getHealth() > 0) {
                    if (player.getHealth() < Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue() - 1) {
                        player.setHealth(player.getHealth() + 1);
                    } else {
                        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue());
                    }
                    if (random.nextBoolean()) {
                        FoodLevelChangeEvent event = new FoodLevelChangeEvent(player, player.getFoodLevel() - 1);
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) player.setFoodLevel(Math.max(0, player.getFoodLevel() - 1));
                    }
                }
            }
        } else {
            regenTick++;
        }

        //Check for players using sword block
        if (CombatiumLite.swordBlockingDisplay) {
            if (CombatiumLite.swordBlocking) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isSword(player.getActiveItem().getType()))
                        player.sendActionBar(Component.text("Blocking").color(NamedTextColor.GREEN));
                    else player.sendActionBar(Component.text(""));
                }
            }
        }

        if (CombatiumLite.swapAccuracy) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerHitUtil.setLastTickDamage(player, PlayerHitUtil.attackDamage(player.getInventory().getItemInMainHand(), null));
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

        currentTick++;
    }
}
