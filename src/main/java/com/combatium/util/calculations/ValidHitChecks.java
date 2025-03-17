package com.combatium.util.calculations;

import com.combatium.CombatiumLite;
import com.combatium.util.ping.PingManager;
import com.combatium.util.ping.PlayerPingData;
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
            double distance = (playerPingData.getEstimatedPing() / 200.0) + 0.25;

            return nmsPlayer.canInteractWithEntity(nmsVictim, Math.min(distance, 2));
        } catch (Exception e) {
            CombatiumLite.logger.warning("Failed to check if " + attacker.getName() + " can interact with " + victim.getName());
            CombatiumLite.logger.warning("Could not obtain Server Player");
            CombatiumLite.logger.warning("Please report this to the developer.");
            return true;
        }
    }

    public static boolean isValidHit(Player attacker, LivingEntity victim, double leeway) {
        try {
            ServerPlayer nmsPlayer = ((org.bukkit.craftbukkit.entity.CraftPlayer) attacker).getHandle();
            Entity nmsVictim = ((org.bukkit.craftbukkit.entity.CraftLivingEntity) victim).getHandle();

            return nmsPlayer.canInteractWithEntity(nmsVictim, leeway);
        } catch (Exception e) {
            CombatiumLite.logger.warning("Failed to check if " + attacker.getName() + " can interact with " + victim.getName());
            CombatiumLite.logger.warning("Could not obtain Server Player");
            CombatiumLite.logger.warning("Please report this to the developer.");
            return true;
        }
    }
}
