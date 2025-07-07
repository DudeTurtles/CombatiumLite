package com.dudeturtles.combatium;

import com.dudeturtles.combatium.listeners.regen.ExhaustListener;
import com.dudeturtles.combatium.listeners.snapshot.CombatifyListener;
import com.dudeturtles.combatium.listeners.damage.*;
import com.dudeturtles.combatium.listeners.itemFixers.*;
import com.dudeturtles.combatium.listeners.projectile.DispenseTridentListener;
import com.dudeturtles.combatium.listeners.projectile.ProjectileHitListener;
import com.dudeturtles.combatium.listeners.projectile.ProjectileThrowListener;
import com.dudeturtles.combatium.listeners.regen.EntityRegainHealthListener;
import com.dudeturtles.combatium.listeners.snapshot.AirSwingListener;
import com.dudeturtles.combatium.listeners.snapshot.DefaultedListener;
import com.dudeturtles.combatium.listeners.snapshot.LiarListener;
import com.dudeturtles.combatium.listeners.util.*;
import com.dudeturtles.combatium.listeners.util.ping.AttributeChangeListener;
import com.dudeturtles.combatium.listeners.util.ping.JoinQuitListener;
import com.dudeturtles.combatium.listeners.util.ping.PingReceiveListener;
import com.dudeturtles.combatium.listeners.util.sweepingEdgeListeners.ItemDropListener;
import com.dudeturtles.combatium.listeners.util.sweepingEdgeListeners.PlayerInteractListener;
import com.dudeturtles.combatium.scheduler.InvulnerabilityScheduler;
import com.dudeturtles.combatium.scheduler.Scheduler;
import com.dudeturtles.combatium.util.ItemPatcher;
import com.dudeturtles.combatium.util.active.PlayerDamageEntity;
import com.dudeturtles.combatium.util.active.ReachUtil;
import com.dudeturtles.combatium.util.calculations.PlayerHitUtil;
import com.dudeturtles.combatium.util.ping.PingRunnable;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Logger;

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
    public Scheduler scheduler;
    private ReachUtil reachUtil;

    //todo: REMOVE THIS
    public static boolean debug = false;

    @Override
    public void onEnable() {
        INSTANCE = this;
        pluginManager = getServer().getPluginManager();
        MobEffects.STRENGTH.value().addAttributeModifier(Attributes.ATTACK_DAMAGE,
                ResourceLocation.withDefaultNamespace("effect.strength"),
                0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        MobEffects.WEAKNESS.value().addAttributeModifier(Attributes.ATTACK_DAMAGE,
                ResourceLocation.withDefaultNamespace("effect.weakness"),
                -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        this.reachUtil = new ReachUtil();
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
        tridentsReturnFromVoid = getConfig().getBoolean("pvp.tridentsReturnFromVoid", true);
        swapAccuracy = getConfig().getBoolean("pvp.swapAccuracy", true);
        sweepOnMiss = getConfig().getBoolean("pvp.sweepOnMiss", true);
        fixSweepOnThrow = getConfig().getBoolean("pvp.fixSweepOnThrow", true);
        shieldProtectionArc = Mth.clamp(getConfig().getDouble("pvp.shieldProtectionArc", 65), 0, 180);
        getReachUtil().setRateLimitTicks(getConfig().getInt("pvp.rateLimitTicks", 4));
        reachUtil.setMissTime(getConfig().getInt("pvp.missTime", 4));
        int regenTickDelay = getConfig().getInt("pvp.regenTickDelay", 39);
        debug = getConfig().getBoolean("pvp.debug", false);

        ItemPatcher.patch();
        printConfig();

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();

        PacketEvents.getAPI().getEventManager().registerListeners(
                new AttributeChangeListener(),
                new PingReceiveListener()
        );

        PacketEvents.getAPI().getEventManager().registerListener(new AirSwingListener(), PacketListenerPriority.LOWEST);
        PacketEvents.getAPI().getEventManager().registerListener(new LiarListener(), PacketListenerPriority.LOWEST);
        pluginManager.registerEvents(new JoinQuitListener(), this);
        PacketEvents.getAPI().getSettings()
                .checkForUpdates(false)
                .debug(false);
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().init();
        pingTask = new PingRunnable().runTaskTimerAsynchronously(this, 0L, 5L);

        PlayerHitUtil.setBackShots(backShotsPlus);

        this.scheduler = new Scheduler(this, regenTickDelay);
        this.scheduler.register(new InvulnerabilityScheduler(this.scheduler));

        EntityDamageByEntityListener entityDamageByEntityListener = new EntityDamageByEntityListener();
        PlayerDamageEntity.setEntityDamageByEntityListener(entityDamageByEntityListener);
        pluginManager.registerEvents(entityDamageByEntityListener, this);
        pluginManager.registerEvents(new PlayerShieldDisableListener(), this);
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
        pluginManager.registerEvents(new TickListener(this.scheduler), this);
        pluginManager.registerEvents(new DispenseTridentListener(), this);
        pluginManager.registerEvents(new AnimationListener(), this);
        pluginManager.registerEvents(new KnockbackListener(), this);
        pluginManager.registerEvents(new JoinQuitListener(), this);
        pluginManager.registerEvents(new ConsumeListener(), this);
        pluginManager.registerEvents(new VehicleDamageListener(), this);
        pluginManager.registerEvents(new ItemDropListener(), this);
        pluginManager.registerEvents(new PrePlayerAttackEvent(), this);
        pluginManager.registerEvents(new ExhaustListener(), this);

        if (combatifySupport) {
            CombatifyListener combatifyListener = new CombatifyListener();
            pluginManager.registerEvents(combatifyListener, this);
            getServer().getMessenger().registerIncomingPluginChannel(this, "combatify:miss_attack", combatifyListener);
        }
        pluginManager.registerEvents(new DefaultedListener(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, ClientboundDefaultComponentsSyncPacket.TYPE.id().toString());
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    private void printConfig() {
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

    public static Scheduler getScheduler() {
        return getInstance().scheduler;
    }

    public static Logger getPluginLogger() {
        return logger;
    }

    public static ReachUtil getReachUtil() {
        return getInstance().reachUtil;
    }
}
