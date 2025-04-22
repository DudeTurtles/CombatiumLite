package com.combatium;

import com.combatium.listeners.snapshot.CombatifyListener;
import com.combatium.listeners.damage.*;
import com.combatium.listeners.itemFixers.*;
import com.combatium.listeners.projectile.DispenseTridentListener;
import com.combatium.listeners.projectile.ProjectileHitListener;
import com.combatium.listeners.projectile.ProjectileThrowListener;
import com.combatium.listeners.regen.EntityRegainHealthListener;
import com.combatium.listeners.snapshot.AirSwingListener;
import com.combatium.listeners.snapshot.KickListener;
import com.combatium.listeners.snapshot.LiarListener;
import com.combatium.listeners.util.*;
import com.combatium.listeners.util.ping.AttributeChangeListener;
import com.combatium.listeners.util.ping.JoinQuitListener;
import com.combatium.listeners.util.ping.PingReceiveListener;
import com.combatium.listeners.util.sweepingEdgeListeners.ItemDropListener;
import com.combatium.listeners.util.sweepingEdgeListeners.PlayerInteractListener;
import com.combatium.scheduler.Scheduler;
import com.combatium.util.active.ReachUtil;
import com.combatium.util.calculations.PlayerHitUtil;
import com.combatium.util.ping.PingRunnable;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import java.util.logging.Logger;

import static com.combatium.util.ItemPatcher.patch;

@SuppressWarnings("unused")
public final class CombatiumLite extends JavaPlugin {
    private static CombatiumLite INSTANCE;
    public static PluginManager pluginManager;
    public static Logger logger = Logger.getLogger("[CombatiumLite]");
    public static boolean backShotsPlus;
    public static boolean gracePeriod;
    public static boolean limitedChargedWeapons;
    public static boolean improvedAnimations;
    public static boolean improvedChargedHits;
    public static boolean rateLimit;
    public static boolean tridentsReturnFromVoid;
    public static boolean swordBlocking;
    public static boolean swordBlockingDisplay;
    public static boolean weakerSpamCrits;
    public static boolean hasteFix;
    public static boolean pingImprovements;
    public static boolean swapAccuracy;
    public static boolean hitThroughTransparent;
    public static boolean saturationChanges;
    public static boolean sweepOnMiss;
    public static boolean fixSweepOnThrow;
    public static double shieldProtectionArc;
    public static boolean sprintFix;
    public static boolean combatifySupport;
    BukkitTask pingTask;

    @Override
    public void onEnable() {
        INSTANCE = this;
        pluginManager = getServer().getPluginManager();
        logger = getLogger();
        MobEffects.DAMAGE_BOOST.value().addAttributeModifier(Attributes.ATTACK_DAMAGE,
                ResourceLocation.withDefaultNamespace("effect.strength"),
                0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        MobEffects.WEAKNESS.value().addAttributeModifier(Attributes.ATTACK_DAMAGE,
                ResourceLocation.withDefaultNamespace("effect.weakness"),
                -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        //Check for config options
        saveDefaultConfig();
        backShotsPlus = getConfig().getBoolean("pvp.backShotsPlus", false);
        swordBlocking = getConfig().getBoolean("pvp.swordBlocking", false);
        swordBlockingDisplay = getConfig().getBoolean("pvp.swordBlockingDisplay", false);
        weakerSpamCrits = getConfig().getBoolean("pvp.weakerSpamCrits", false);
        hasteFix = getConfig().getBoolean("pvp.hasteFix", false);
        hitThroughTransparent = getConfig().getBoolean("pvp.hitThroughTransparent", false);
        sprintFix = getConfig().getBoolean("pvp.sprintFix", false);
        combatifySupport = getConfig().getBoolean("pvp.combatifySupport", false);
        saturationChanges = getConfig().getBoolean("pvp.saturationChanges", true);
        gracePeriod = getConfig().getBoolean("pvp.gracePeriod", true);
        improvedAnimations = getConfig().getBoolean("pvp.improvedAnimations", true);
        improvedChargedHits = getConfig().getBoolean("pvp.improvedChargedHits", true);
        pingImprovements = getConfig().getBoolean("pvp.pingImprovements", true);
        rateLimit = getConfig().getBoolean("pvp.rateLimit", true);
        tridentsReturnFromVoid = getConfig().getBoolean("pvp.tridentsReturnFromVoid", true);
        swapAccuracy = getConfig().getBoolean("pvp.swapAccuracy", true);
        sweepOnMiss = getConfig().getBoolean("pvp.sweepOnMiss", true);
        fixSweepOnThrow = getConfig().getBoolean("pvp.fixSweepOnThrow", true);
        shieldProtectionArc = getConfig().getDouble("pvp.shieldProtectionArc", 130);
        ReachUtil.setRateLimitTicks(getConfig().getInt("pvp.rateLimitTicks", 3));
        ReachUtil.setMissTime(getConfig().getInt("pvp.missTime", 4));
        int regenTickDelay = getConfig().getInt("pvp.regenTickDelay", 39);

        patch();
        printConfig();

        if (pingImprovements) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().load();

            PacketEvents.getAPI().getEventManager().registerListeners(
                    new AttributeChangeListener(),
                    new PingReceiveListener()
            );

            PacketEvents.getAPI().getEventManager().registerListener(new AirSwingListener(), PacketListenerPriority.LOWEST);
            PacketEvents.getAPI().getEventManager().registerListener(new KickListener(), PacketListenerPriority.LOWEST);
            PacketEvents.getAPI().getEventManager().registerListener(new LiarListener(), PacketListenerPriority.LOWEST);
            pluginManager.registerEvents(new JoinQuitListener(), this);
            PacketEvents.getAPI().getSettings()
                    .checkForUpdates(false)
                    .debug(false);
            PacketEvents.getAPI().load();
            PacketEvents.getAPI().init();
            pingTask = new PingRunnable().runTaskTimerAsynchronously(this, 0L, 5L);
        }

        PlayerHitUtil.setBackShots(backShotsPlus);

        pluginManager.registerEvents(new EntityDamageByEntityListener(), this);
        pluginManager.registerEvents(new EntityDamageListener(), this);
        pluginManager.registerEvents(new ProjectileHitListener(), this);
        pluginManager.registerEvents(new EntityDeathListener(), this);
        pluginManager.registerEvents(new EntityRegainHealthListener(), this);
        pluginManager.registerEvents(new DisconnectListener(), this);
        pluginManager.registerEvents(new PlayerInteractListener(), this);
        pluginManager.registerEvents(new PlayerJoinListener(), this);
        pluginManager.registerEvents(new ProjectileThrowListener(), this);
        pluginManager.registerEvents(new PlayerInteractListenerFix(), this);
        pluginManager.registerEvents(new CraftItemListener(), this);
        pluginManager.registerEvents(new TickListener(new Scheduler(this, regenTickDelay)), this);
        pluginManager.registerEvents(new DispenseTridentListener(), this);
        pluginManager.registerEvents(new AnimationListener(), this);
        pluginManager.registerEvents(new KnockbackListener(), this);
        pluginManager.registerEvents(new JoinQuitListener(), this);
        pluginManager.registerEvents(new ConsumeListener(), this);
        pluginManager.registerEvents(new VehicleDamageListener(), this);
        pluginManager.registerEvents(new ItemDropListener(), this);
        pluginManager.registerEvents(new PlayerDeathListener(), this);

        if (combatifySupport) {
            CombatifyListener combatifyListener = new CombatifyListener();
            pluginManager.registerEvents(combatifyListener, this);
            getServer().getMessenger().registerIncomingPluginChannel(this, "combatify:miss_attack", combatifyListener);
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    private static void printConfig() {
        logger.log(java.util.logging.Level.INFO, "=== CombatiumLite Config ===");
        logger.log(java.util.logging.Level.INFO, "Back Shots Plus: " + backShotsPlus);
        logger.log(java.util.logging.Level.INFO, "Sword Blocking: " + swordBlocking);
        logger.log(java.util.logging.Level.INFO, "Weaker Spam Crits: " + weakerSpamCrits);
        logger.log(java.util.logging.Level.INFO, "Hit Through Transparent: " + hitThroughTransparent);
        logger.log(java.util.logging.Level.INFO, "Saturation Changes: " + saturationChanges);
        logger.log(java.util.logging.Level.INFO, "Grace Period: " + gracePeriod);
        logger.log(java.util.logging.Level.INFO, "Improved Animations: " + improvedAnimations);
        logger.log(java.util.logging.Level.INFO, "Improved Charged Hits: " + improvedChargedHits);
        logger.log(java.util.logging.Level.INFO, "Ping Improvements: " + pingImprovements);
        logger.log(java.util.logging.Level.INFO, "Rate Limit: " + rateLimit);
        logger.log(java.util.logging.Level.INFO, "Tridents Return From Void: " + tridentsReturnFromVoid);
        logger.log(java.util.logging.Level.INFO, "Swap Accuracy: " + swapAccuracy);
        logger.log(java.util.logging.Level.INFO, "Sweep On Miss: " + sweepOnMiss);
        logger.log(java.util.logging.Level.INFO, "Fix Sweep On Throw: " + fixSweepOnThrow);
        logger.log(java.util.logging.Level.INFO, "Shield Protection Arc: " + shieldProtectionArc);
        logger.log(java.util.logging.Level.INFO, "Sprint Fix: " + sprintFix);
        logger.log(java.util.logging.Level.INFO, "Combatify Support: " + combatifySupport);
    }

    public static CombatiumLite getInstance() {
        return INSTANCE;
    }
}
