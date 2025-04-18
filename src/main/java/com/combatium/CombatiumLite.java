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
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.UseCooldown;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class CombatiumLite extends JavaPlugin {
    private static CombatiumLite INSTANCE;
    public static PluginManager pluginManager;
    public static Logger logger = Logger.getLogger("[CombatiumLite]");
    public static boolean backShotsPlus;
    public static boolean visualCooldownChanges;
    public static boolean stackablePotions;
    public static boolean updateItemReach;
    public static boolean updateLore;
    public static boolean cooldownShowsFullCharge;
    public static boolean gracePeriod;
    public static boolean limitedChargedWeapons;
    public static boolean improvedAnimations;
    public static boolean improvedChargedHits;
    public static boolean rateLimit;
    public static boolean tridentsReturnFromVoid;
    public static boolean swordBlocking;
    public static boolean swordBlockingDisplay;
    public static boolean weakerSpamCrits;
    public static boolean pingImprovements;
    public static boolean swapAccuracy;
    public static boolean fasterLiquidDrinking;
    public static boolean hitThroughTransparent;
    public static boolean saturationChanges;
    public static boolean sweepOnMiss;
    public static boolean fixSweepOnThrow;
    public static double shieldProtectionArc;
    public static boolean sprintFix;
    public static boolean combatifySupport;
    public static boolean supportExternalWeaponMods;
    BukkitTask pingTask;
    public static final ResourceLocation BASE_ATTACK_SPEED_CTS_ID = ResourceLocation.withDefaultNamespace("base_attack_speed_cts");
    public static final ResourceLocation BASE_ATTACK_REACH_ID = ResourceLocation.withDefaultNamespace("base_attack_reach");

    @Override
    public void onEnable() {
        INSTANCE = this;
        pluginManager = getServer().getPluginManager();
        logger = getLogger();
        MobEffects.WEAKNESS.value().addAttributeModifier(Attributes.ATTACK_DAMAGE,
                ResourceLocation.withDefaultNamespace("effect.weakness"),
                -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        //Check for config options
        saveDefaultConfig();
        backShotsPlus = getConfig().getBoolean("pvp.backShotsPlus", false);
        stackablePotions = getConfig().getBoolean("pvp.stackablePotions", true);
        visualCooldownChanges = getConfig().getBoolean("pvp.visualCooldownChanges", true);
        swordBlocking = getConfig().getBoolean("pvp.swordBlocking", false);
        swordBlockingDisplay = getConfig().getBoolean("pvp.swordBlockingDisplay", false);
        weakerSpamCrits = getConfig().getBoolean("pvp.weakerSpamCrits", false);
        hitThroughTransparent = getConfig().getBoolean("pvp.hitThroughTransparent", false);
        sprintFix = getConfig().getBoolean("pvp.sprintFix", false);
        combatifySupport = getConfig().getBoolean("pvp.combatifySupport", false);
        supportExternalWeaponMods = getConfig().getBoolean("pvp.supportExternalWeaponMods", false);
        saturationChanges = getConfig().getBoolean("pvp.saturationChanges", true);
        updateLore = getConfig().getBoolean("pvp.updateLore", true);
        updateItemReach = getConfig().getBoolean("pvp.updateItemReach", true);
        cooldownShowsFullCharge = getConfig().getBoolean("pvp.cooldownShowsFullCharge", true);
        gracePeriod = getConfig().getBoolean("pvp.gracePeriod", true);
        improvedAnimations = getConfig().getBoolean("pvp.improvedAnimations", true);
        improvedChargedHits = getConfig().getBoolean("pvp.improvedChargedHits", true);
        pingImprovements = getConfig().getBoolean("pvp.pingImprovements", true);
        rateLimit = getConfig().getBoolean("pvp.rateLimit", true);
        tridentsReturnFromVoid = getConfig().getBoolean("pvp.tridentsReturnFromVoid", true);
        swapAccuracy = getConfig().getBoolean("pvp.swapAccuracy", true);
        fasterLiquidDrinking = getConfig().getBoolean("pvp.fasterLiquidDrinking", true);
        sweepOnMiss = getConfig().getBoolean("pvp.sweepOnMiss", true);
        fixSweepOnThrow = getConfig().getBoolean("pvp.fixSweepOnThrow", true);
        shieldProtectionArc = getConfig().getDouble("pvp.shieldProtectionArc", 130);
        ReachUtil.setRateLimitTicks(getConfig().getInt("pvp.rateLimitTicks", 3));
        ReachUtil.setMissTime(getConfig().getInt("pvp.missTime", 4));
        int regenTickDelay = getConfig().getInt("pvp.regenTickDelay", 39);

        try {
            Field streamCodec = ItemStack.class.getDeclaredField("OPTIONAL_STREAM_CODEC");
            removeFinalMarker(streamCodec);

            streamCodec.set(null, ItemStack.OPTIONAL_STREAM_CODEC.map(stack -> {
                if (!stack.isEmpty()) {
                    DataComponentMap prototype = stack.getPrototype();
                    if (prototype instanceof PatchedDataComponentMap prototypeDataComponentMap) {
                        ItemStack newStack = stack.copy();
                        if (newStack.getComponents() instanceof PatchedDataComponentMap patchedDataComponentMap) {
                            patchedDataComponentMap.restorePatch(prototypeDataComponentMap.asPatch());
                            patchedDataComponentMap.applyPatch(stack.getComponentsPatch());
                        }
                        stack = newStack;
                    }
                }
                return stack;
            }, Function.identity()));
            // Patch components here
            if (stackablePotions) applyDefaultComponents(size(16), Items.POTION);
            applyDefaultComponents(size(64).andThen(cooldown(0.2F)), Items.SNOWBALL);
            applyDefaultComponents(cooldown(0.2F), Items.EGG);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to update default components!" + e, e);
        }

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
        pluginManager.registerEvents(new EntityAttackListener(visualCooldownChanges, updateItemReach), this);
        pluginManager.registerEvents(new PlayerInteractListenerFix(visualCooldownChanges, updateItemReach), this);
        pluginManager.registerEvents(new ItemPickupListener(visualCooldownChanges, updateItemReach), this);
        pluginManager.registerEvents(new InventoryClickListener(visualCooldownChanges, updateItemReach), this);
        pluginManager.registerEvents(new CraftItemListener(visualCooldownChanges, updateItemReach), this);
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
        logger.log(java.util.logging.Level.INFO, "Stackable Potions: " + stackablePotions);
        logger.log(java.util.logging.Level.INFO, "Visual Cooldown Changes: " + visualCooldownChanges);
        logger.log(java.util.logging.Level.INFO, "Sword Blocking: " + swordBlocking);
        logger.log(java.util.logging.Level.INFO, "Weaker Spam Crits: " + weakerSpamCrits);
        logger.log(java.util.logging.Level.INFO, "Hit Through Transparent: " + hitThroughTransparent);
        logger.log(java.util.logging.Level.INFO, "Saturation Changes: " + saturationChanges);
        logger.log(java.util.logging.Level.INFO, "Update Lore: " + updateLore);
        logger.log(java.util.logging.Level.INFO, "Update Item Reach: " + updateItemReach);
        logger.log(java.util.logging.Level.INFO, "Cooldown Shows Full Charge: " + cooldownShowsFullCharge);
        logger.log(java.util.logging.Level.INFO, "Grace Period: " + gracePeriod);
        logger.log(java.util.logging.Level.INFO, "Improved Animations: " + improvedAnimations);
        logger.log(java.util.logging.Level.INFO, "Improved Charged Hits: " + improvedChargedHits);
        logger.log(java.util.logging.Level.INFO, "Ping Improvements: " + pingImprovements);
        logger.log(java.util.logging.Level.INFO, "Rate Limit: " + rateLimit);
        logger.log(java.util.logging.Level.INFO, "Tridents Return From Void: " + tridentsReturnFromVoid);
        logger.log(java.util.logging.Level.INFO, "Swap Accuracy: " + swapAccuracy);
        logger.log(java.util.logging.Level.INFO, "Faster Liquid Drinking: " + fasterLiquidDrinking);
        logger.log(java.util.logging.Level.INFO, "Sweep On Miss: " + sweepOnMiss);
        logger.log(java.util.logging.Level.INFO, "Fix Sweep On Throw: " + fixSweepOnThrow);
        logger.log(java.util.logging.Level.INFO, "Shield Protection Arc: " + shieldProtectionArc);
        logger.log(java.util.logging.Level.INFO, "Sprint Fix: " + sprintFix);
        logger.log(java.util.logging.Level.INFO, "Combatify Support: " + combatifySupport);
        logger.log(java.util.logging.Level.INFO, "Support External Weapon Mods: " + supportExternalWeaponMods);
    }

    public static CombatiumLite getInstance() {
        return INSTANCE;
    }

    public static void applyDefaultComponents(Consumer<PatchedDataComponentMap> patcher, Item item) throws NoSuchFieldException, IllegalAccessException {
        Field components = Item.class.getDeclaredField("components");
        components.setAccessible(true);

        removeFinalMarker(components);

        PatchedDataComponentMap newMap = new PatchedDataComponentMap(item.components());
        patcher.accept(newMap);
        components.set(item, newMap);
    }
    public static void removeFinalMarker(Field field) throws NoSuchFieldException, IllegalAccessException {
        Field fieldModifiers = Field.class.getDeclaredField("modifiers");
        fieldModifiers.setAccessible(true);
        fieldModifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
    public static Consumer<PatchedDataComponentMap> weapon(float damage, float speed, float reach) {
        return patchedDataComponentMap -> {
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_CTS_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            builder.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ATTACK_REACH_ID, reach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            patchedDataComponentMap.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build().withTooltip(false));
        };
    }
    public static Consumer<PatchedDataComponentMap> size(int stackSize) {
        return patchedDataComponentMap -> patchedDataComponentMap.set(DataComponents.MAX_STACK_SIZE, stackSize);
    }
    public static Consumer<PatchedDataComponentMap> cooldown(float seconds) {
        return patchedDataComponentMap -> patchedDataComponentMap.set(DataComponents.USE_COOLDOWN, new UseCooldown(seconds));
    }
}
