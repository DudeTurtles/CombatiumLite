package com.dudeturtles.combatium.util.calculations;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.util.ping.PingManager;
import com.dudeturtles.combatium.util.ping.PlayerPingData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ValidHitChecks {
    public static boolean isValidHit(Player attacker, LivingEntity victim) {
        try {
            ServerPlayer nmsPlayer = ((org.bukkit.craftbukkit.entity.CraftPlayer) attacker).getHandle();
            Entity nmsVictim = ((org.bukkit.craftbukkit.entity.CraftLivingEntity) victim).getHandle();

            PlayerPingData playerPingData = PingManager.getPlayerData(attacker.getUniqueId());
            double distance = (playerPingData != null) ? (playerPingData.getEstimatedPingNoOffset() / 200.0) + 0.25 : 0;

            return nmsPlayer.canInteractWithEntity(nmsVictim, Math.min(distance, 2));
        } catch (Exception e) {
            CombatiumLite.getPluginLogger().warning("Failed to check if " + attacker.getName() + " can interact with " + victim.getName());
            CombatiumLite.getPluginLogger().warning("Could not obtain Server Player");
            CombatiumLite.getPluginLogger().warning("Please report this to the developer.");
            return true;
        }
    }

    public static boolean isValidHit(Player attacker, LivingEntity victim, double leeway) {
        try {
            ServerPlayer nmsPlayer = ((org.bukkit.craftbukkit.entity.CraftPlayer) attacker).getHandle();
            Entity nmsVictim = ((org.bukkit.craftbukkit.entity.CraftLivingEntity) victim).getHandle();

            return nmsPlayer.canInteractWithEntity(nmsVictim, leeway);
        } catch (Exception e) {
            CombatiumLite.getPluginLogger().warning("Failed to check if " + attacker.getName() + " can interact with " + victim.getName());
            CombatiumLite.getPluginLogger().warning("Could not obtain Server Player");
            CombatiumLite.getPluginLogger().warning("Please report this to the developer.");
            return true;
        }
    }
}
