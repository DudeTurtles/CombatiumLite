package com.dudeturtles.combatium.scheduler;

import com.dudeturtles.combatium.CombatiumLite;
import com.dudeturtles.combatium.util.active.PlayerDamageEntity;
import com.dudeturtles.combatium.util.calculations.ValidHitChecks;
import net.minecraft.sounds.SoundEvents;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class ScheduledHit {
    Player attacker;
    LivingEntity target;
    EntityDamageEvent.DamageCause damageCause;

    public ScheduledHit(Player attacker, LivingEntity target, EntityDamageEvent.DamageCause damageCause) {
        this.attacker = attacker;
        this.target = target;
        this.damageCause = damageCause;
    }

    public void execute() {
        ItemStack weapon = attacker.getInventory().getItemInMainHand();

        if (!ValidHitChecks.isValidHit(attacker, target)) {
            CombatiumLite.getReachUtil().resetCooldown(attacker, true);
            return;
        }
        if (PlayerDamageEntity.playerDamageEntityEvent(attacker, target, weapon, damageCause, true))
            PlayerDamageEntity.playServerSideSound(((CraftPlayer) attacker).getHandle(), SoundEvents.PLAYER_ATTACK_NODAMAGE);
        net.minecraft.world.entity.LivingEntity nmsAttacker = ((CraftLivingEntity) attacker).getHandle();
        nmsAttacker.swing(nmsAttacker.swingingArm, true);
    }
}
