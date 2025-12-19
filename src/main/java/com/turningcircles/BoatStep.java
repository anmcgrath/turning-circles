package com.turningcircles;

import net.runelite.api.coords.LocalPoint;

public class BoatStep {
    public LocalPoint location;
    public int orientation;

    public BoatStep(LocalPoint location, int orientation) {
        this.location = location;
        this.orientation = orientation;
    }
}
