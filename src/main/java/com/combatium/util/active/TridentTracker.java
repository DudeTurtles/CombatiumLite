package com.combatium.util.active;

import com.combatium.CombatiumLite;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Trident;
import org.bukkit.projectiles.BlockProjectileSource;

import java.util.ArrayList;

public class TridentTracker {
    private static final ArrayList<Trident> tridents = new ArrayList<>();

    public static void checkTridents() {
        if (!CombatiumLite.tridentsReturnFromVoid) return;
        tridents.removeIf(trident -> (trident.isDead() || !trident.getItemStack().containsEnchantment(Enchantment.LOYALTY) ||
                trident.isInBlock() || trident.isOnGround() || !(trident.isValid()) || trident.getShooter() == null ||
                trident.getShooter() instanceof BlockProjectileSource || trident.hasDealtDamage()));

        for (Trident trident : tridents) {
            if (trident.getLocation().getY() <= trident.getWorld().getMinHeight()) {
                trident.setVelocity(trident.getVelocity().multiply(0));
                trident.setGravity(false);
                trident.setHasDealtDamage(true);
            }
        }
    }

    public static void addTrident(Trident trident) {
        if (CombatiumLite.tridentsReturnFromVoid) tridents.add(trident);
    }


}
