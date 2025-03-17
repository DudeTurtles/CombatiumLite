package com.combatium.util.hitThroughTransparent;

import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;

public class PlayerData {
    private final Player player;
    private final ArrayList<BoundingBox> boxHistory = new ArrayList<>();

    public PlayerData(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void update() {
        addBox(player.getBoundingBox());
    }

    private void addBox(BoundingBox box) {
        if (boxHistory.size() > 5) {
            boxHistory.removeFirst();
        }
        boxHistory.add(box);
    }

    /* Retrieves the bounding box which is nearest to the delay specified in ticks */
    public BoundingBox getNearestBox(int ticks) {
        if (boxHistory.size() < ticks - 1) {
            return boxHistory.getLast();
        }

        return boxHistory.get(Math.max(ticks - 1, 0));
    }
}
