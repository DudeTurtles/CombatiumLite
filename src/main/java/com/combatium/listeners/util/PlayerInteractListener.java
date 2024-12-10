package com.combatium.listeners.util;

import com.combatium.util.active.ReachUtil;
import com.combatium.util.active.SweepingEdgeUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import static org.bukkit.event.block.Action.LEFT_CLICK_AIR;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().isLeftClick()) {
            if (ReachUtil.isCharged(event.getPlayer())) {
                if (event.getAction() == LEFT_CLICK_AIR && ReachUtil.isOverCharged(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand().getType(), ReachUtil.getHaste(event.getPlayer()))
                        && (SweepingEdgeUtil.getSweepingLevel(event.getPlayer()) > 0)) {
                    SweepingEdgeUtil.Sweep(event.getPlayer(), null);
                }

                if (!(ReachUtil.isRateLimited(event.getPlayer()))) {
                    ReachUtil.resetCooldown(event.getPlayer(), true, event.getPlayer().getInventory().getItemInMainHand().getType());
                } else {
                    AnimationListener.cancelAnim(event.getPlayer());
                }
            } else if (event.getAction() == LEFT_CLICK_AIR) {
                AnimationListener.cancelAnim(event.getPlayer());
            }
        }
    }
}
