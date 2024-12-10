package com.combatium.scheduler;

import com.combatium.CombatiumLite;
import com.combatium.util.active.PlayerDamageEntity;
import com.combatium.util.active.ReachUtil;
import com.combatium.util.active.TridentTracker;
import com.combatium.util.calculations.PlayerHitUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.combatium.util.calculations.FixItem.isSword;

public class Scheduler {
    static Plugin plugin;
    static int currentTick;
    static int regenTick;
    //The amount of ticks between each regen tick (Default 39)
    static int regenTickDelay;
    static ArrayList<ScheduledHit> scheduledHits;
    static ArrayList<ScheduledHit> toRemove;
    private static final Map<Player, Integer> regenExhaustionMap = new HashMap<>();

    public Scheduler(Plugin plugin, int regenTickDelay) {
        Scheduler.plugin = plugin;
        Scheduler.regenTickDelay = regenTickDelay;
        currentTick = 0;
        regenTick = 0;
        scheduledHits = new ArrayList<>();
        toRemove = new ArrayList<>();
    }

    public static int getCurrentTick() {
        return currentTick;
    }

    public static void scheduleHit(ScheduledHit hit) {
        scheduledHits.add(hit);
    }

    public void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setNoDamageTicks(0);
        }
        //Update the reach of each player
        for (Player player : Bukkit.getOnlinePlayers()) {
            ReachUtil.updatePlayerReach(player);
        }

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
                if (player.getHealth() < Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue() &&
                        player.getFoodLevel() > 7 && player.getHealth() > 0) {
                    if (player.getHealth() < Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue() - 1) {
                        player.setHealth(player.getHealth() + 1);
                    } else {
                        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue());
                    }
                    addExhaustion(player, 3);
                }
            }
        } else {
            regenTick++;
        }

        //Check for players using sword block
        if (CombatiumLite.swordBlocking) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isSword(player.getActiveItem().getType()))
                    player.sendActionBar(Component.text("Blocking").color(NamedTextColor.GREEN));
                else player.sendActionBar(Component.text(""));
            }
        }

        if (CombatiumLite.swapAccuracy) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerHitUtil.setLastTickDamage(player, PlayerHitUtil.attackDamage(player.getInventory().getItemInMainHand(), null));
                PlayerDamageEntity.setLastTickWeapon(player, player.getInventory().getItemInMainHand().getType());
            }
        }

        if (CombatiumLite.tridentsReturnFromVoid) {
            TridentTracker.checkTridents();
        }

        currentTick++;
    }

    private static void addExhaustion(Player player, int exhaustion) {
        int exhaustionValue = regenExhaustionMap.getOrDefault(player, 0);
        exhaustionValue += exhaustion;
        if (exhaustionValue > 3) {
            exhaustionValue -= 4;
            /*
             * Call an event to ensure the hunger loss is cancellable.
             */
            FoodLevelChangeEvent event = new FoodLevelChangeEvent(player, player.getFoodLevel() > 1 ? player.getFoodLevel() - 1 : 0, new ItemStack(Material.WAXED_WEATHERED_CUT_COPPER_STAIRS));
            Bukkit.getPluginManager().callEvent(event);
        }
        regenExhaustionMap.put(player, exhaustionValue);
    }
}
