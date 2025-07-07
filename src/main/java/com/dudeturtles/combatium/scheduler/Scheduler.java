package com.dudeturtles.combatium.scheduler;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.util.active.PlayerDamageEntity;
import com.dudeturtles.combatium.util.active.ReachUtil;
import com.dudeturtles.combatium.util.active.TridentTracker;
import com.dudeturtles.combatium.util.calculations.FixItem;
import com.dudeturtles.combatium.util.calculations.PlayerHitUtil;
import com.dudeturtles.combatium.util.hitThroughTransparent.HitManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
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

    public Scheduler(Plugin plugin, int regenTickDelay) {
        this.plugin = plugin;
        this.regenTickDelay = regenTickDelay;
        currentTick = 0;
        regenTick = 0;
        scheduledSweeps = new ArrayList<>();
        scheduledHits = new ArrayList<>();
        toRemove = new ArrayList<>();
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
                if (!(player.getWorld().getGameRuleValue(GameRule.NATURAL_REGENERATION) && nmsPlayer.isHurt() && player.getFoodLevel() > 7)) continue;

                nmsPlayer.heal(1.0F, EntityRegainHealthEvent.RegainReason.CUSTOM);

                if (!nmsPlayer.getRandom().nextBoolean()) continue;
                FoodLevelChangeEvent event = new FoodLevelChangeEvent(player, player.getFoodLevel() - 1, null);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) player.setFoodLevel(Math.max(0, player.getFoodLevel() - 1));
            }
        }

        //Check for players using sword block
        if (CombatiumLite.swordBlockingDisplay && CombatiumLite.swordBlocking) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (FixItem.isSword(player.getActiveItem().getType())) player.sendActionBar(Component.text("Blocking").color(NamedTextColor.GREEN));
                else player.sendActionBar(Component.text(""));
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

        currentTick++;
    }

    public void register(InvulnerabilityScheduler invulnerabilityScheduler) {
        this.invulnerabilityScheduler = invulnerabilityScheduler;
    }

    public InvulnerabilityScheduler getInvulnerabilityScheduler() {
        return invulnerabilityScheduler;
    }
}
