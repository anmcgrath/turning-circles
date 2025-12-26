package com.turningcircles;

import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;

public class BoatStep {
    public Point offset;
    public int orientation;

    public BoatStep(Point offset, int orientation) {
        this.offset = offset;
        this.orientation = orientation;
    }
}
