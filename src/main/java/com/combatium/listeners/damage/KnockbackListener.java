package com.combatium.listeners.damage;

import com.combatium.util.active.KnockbackUtil;
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import java.util.Objects;

public class KnockbackListener implements Listener {
    @EventHandler
    public void onKnockback(EntityKnockbackByEntityEvent event) {
        EntityKnockbackEvent.Cause cause = event.getCause();
        if (!(cause == EntityKnockbackEvent.Cause.ENTITY_ATTACK || cause == EntityKnockbackEvent.Cause.SWEEP_ATTACK || cause == EntityKnockbackEvent.Cause.SHIELD_BLOCK)) return;

        double strength = (event.getKnockbackStrength() / (1 - Objects.requireNonNull(event.getEntity().getAttribute(Attribute.KNOCKBACK_RESISTANCE)).getValue()));
        Vector v = event.getEntity().getVelocity();
        Vector k = event.getKnockback().add(v);
        double x = ((v.getX() / 2d) - k.getX()) / event.getKnockbackStrength();
        double z = ((v.getZ() / 2d) - k.getZ()) / event.getKnockbackStrength();

        KnockbackUtil.applyKnockback(event.getEntity(), strength, x, z, true);
        event.setCancelled(true);
    }
}
