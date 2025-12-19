package com.turningcircles;

import org.junit.Assert;
import org.junit.Test;

public class SailingMathTests {
    @Test
    public void orientationToDegreesReturnsCorrectValues() {
        Assert.assertEquals(90, SailingMath.orientationToDegrees(1024), 0.001);
        Assert.assertEquals(270, SailingMath.orientationToDegrees(0), 0.001);
        Assert.assertEquals(22.5, SailingMath.orientationToDegrees(1536 - 128), 0.001);
    }

    @Test
    public void cycleOrientationReturnsCorrectValues() {
        Assert.assertEquals(1024, SailingMath.cycleOrientation(1024), 0.001);
        Assert.assertEquals(0, SailingMath.cycleOrientation(0), 0.001);
        Assert.assertEquals(2048 - 128, SailingMath.cycleOrientation(-128), 0.001);
        Assert.assertEquals(128, SailingMath.cycleOrientation(2048 + 128), 0.001);
    }

    @Test
    public void degreesToOrientationReturnsCorrectValues() {
        Assert.assertEquals(1536, SailingMath.degreesToOrientation(0), 0.001);
        Assert.assertEquals(1024, SailingMath.degreesToOrientation(90), 0.001);
        Assert.assertEquals(0, SailingMath.degreesToOrientation(270), 0.001);
    }

    @Test
    public void calculateOrientationDirectionReturnsCorrectValues() {
        Assert.assertEquals(-1, SailingMath.calculateAngleDirectionBetweenOrientations(0, 1536));
        Assert.assertEquals(1, SailingMath.calculateAngleDirectionBetweenOrientations(1536, 0));
        Assert.assertEquals(1, SailingMath.calculateAngleDirectionBetweenOrientations(1920, 0));
        Assert.assertEquals(1, SailingMath.calculateAngleDirectionBetweenOrientations(512, 1024));
        Assert.assertEquals(1, SailingMath.calculateAngleDirectionBetweenOrientations(0, 512));
    }
}
