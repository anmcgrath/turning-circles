package com.turningcircles;

import net.runelite.api.coords.LocalPoint;

public class SailingMath {
    public static int roundToQuarterTile(int offset) {
        return Math.round(offset / 32f) * 32;
    }

    public static double orientationToDegrees(int angle) {
        return 270 - angle / (2048 / 360f);
    }

    public static int degreesToOrientation(double degrees) {
        return (int) (-(degrees - 270) * (2048 / 360f));
    }

    public static int cycleOrientation(int orientation) {
        if (orientation < 0)
            return (2048 + orientation) % 2048;

        return orientation % 2048;
    }

    /**
     * Returns how the angle should change (1 means angle increases, -1 means decreases between two orientations
     *
     * @param start                the first orientation
     * @param target               the target orientation
     * @param currentTurnDirection the current angular speed, for the edge case where we are turning around.
     */
    public static int calculateAngleDirectionBetweenOrientations(int start, int target, int currentTurnDirection) {
        var d1 = orientationToDegrees(start);
        var d2 = orientationToDegrees(target);
        var dA = d2 - d1;
        if (dA < -180)
            return -1;
        else if (dA > 180)
            return 1;
        else if (dA == 180 || dA == -180) {
            // in the case we do a full 180 deg turn, we always turn clockwise
            // unless we are already turning anticlockwise in which case we keep doing that.
            if (currentTurnDirection < 0)
                return -1;
            else
                return 1;
        }
        return -(int) (dA / Math.abs(dA));
    }

    /// speed in frac of tiles which should be a multiple of 0.5
    public static double getSpeed(int vx, int vy) {
        return Math.round(Math.sqrt(Math.pow(vx / 128f, 2) + Math.pow(vy / 128f, 2)) / 0.5f) * 0.5f;
    }

    public static BoatStep[] generateSteps(int nSteps, int fromOrientation, int toOrientation, LocalPoint currentLocation, double currentSpeed, int currentTurnDirection) {
        // show boat movement preview
        var steps = new BoatStep[nSteps];
        var dA = 128;
        int a = fromOrientation;
        var dirA = SailingMath.calculateAngleDirectionBetweenOrientations(a, toOrientation, currentTurnDirection);
        var pos = currentLocation.dx(0).dy(0);
        for (int i = 0; i < nSteps; i++) {
            if (a != toOrientation) {
                a += dA * dirA;
            }
            a = SailingMath.cycleOrientation(a);
            var aDeg = SailingMath.orientationToDegrees(a);
            var vSx = SailingMath.roundToQuarterTile((int) (Math.cos(Math.toRadians(aDeg)) * currentSpeed * 128));
            var vSy = SailingMath.roundToQuarterTile((int) (Math.sin(Math.toRadians(aDeg)) * currentSpeed * 128));
            pos = pos.plus(vSx, vSy);
            steps[i] = new BoatStep(pos, a);
        }

        return steps;
    }
}
