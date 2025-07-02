package com.dudeturtles.combatium.util.hitThroughTransparent;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import java.util.ArrayList;

public class PlayerData {
    private final Player player;
    private final ArrayList<BoundingBox> boxHistory = new ArrayList<>();
    private final ArrayList<Location> eyeLocationHistory = new ArrayList<>();

    public PlayerData(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void update() {
        addBox(player.getBoundingBox());
        addEyeLocation(player.getEyeLocation());
    }

    private void addBox(BoundingBox box) {
        if (boxHistory.size() > 5) {
            boxHistory.removeFirst();
        }
        boxHistory.add(box);
    }

    private void addEyeLocation(Location eyeLocation) {
        if (eyeLocationHistory.size() > 5) {
            eyeLocationHistory.removeFirst();
        }
        eyeLocationHistory.add(eyeLocation);
    }

    /* Retrieves the bounding box which is nearest to the delay specified in ticks */
    public BoundingBox getNearestBox(int ticks) {
        if (boxHistory.size() < ticks - 1) {
            return boxHistory.getLast();
        }

        return boxHistory.get(Math.max(ticks - 1, 0));
    }

    /* Retrieves the eye location which is nearest to the delay specified in ticks */
    public Location getNearestEyeLocation(int ticks) {
        if (eyeLocationHistory.size() < ticks - 1) {
            return eyeLocationHistory.getLast();
        }

        return eyeLocationHistory.get(Math.max(ticks - 1, 0));
    }
}
