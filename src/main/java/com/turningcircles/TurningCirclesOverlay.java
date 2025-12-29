package com.turningcircles;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;

import java.awt.*;

@Slf4j
public class TurningCirclesOverlay extends Overlay {

    private final Client client;
    private final TurningCirclePlugin plugin;
    private final TurningCirclesConfig config;
    private final BoatManager boatManager;

    @Inject
    public TurningCirclesOverlay(
            Client client,
            TurningCirclePlugin plugin,
            TurningCirclesConfig config,
            BoatManager boatManager) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.boatManager = boatManager;
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D g) {
        if (!boatManager.isNavigating())
            return null;

        if (plugin.currentSpeed == 0 && !config.showWhenStopped())
            return null;

        var boat = boatManager.getBoatEntity();
        if (boat == null) return null;

        var bc = boat.getConfig();
        var boatRect = new Rect(bc.getBoundsX(), bc.getBoundsY(), bc.getBoundsWidth(), bc.getBoundsHeight());

        var mouseHeading = plugin.calculateMouseHeading(client, boat.getLocalLocation());

        var paths = generateSteps(
                config.nSteps(),
                boat.getTargetOrientation(),
                mouseHeading);

        var outline = config.renderColor();
        var bg = config.fillColor();

        for (int i = 0; i < paths.length; i++) {
            var path = paths[i];
            var newLoc = boat.getTargetLocation().plus(path.offset.getX(), path.offset.getY());

            var frac = 1 - (double) i / (double) paths.length;

            if (config.fadeOutlineAlpha())
                outline = ColorUtil.colorWithAlpha(outline, (int) ((outline.getAlpha() - config.finalOutlineAlpha()) * frac + config.finalOutlineAlpha()));

            if (config.fadeBackgroundAlpha())
                bg = ColorUtil.colorWithAlpha(bg, (int) ((bg.getAlpha() - config.finalBackgroundAlpha()) * frac + config.finalBackgroundAlpha()));

            renderRotatedRect(client, g, newLoc, boatRect, path.orientation, outline, bg);
        }

        return null;
    }

    private void renderRotatedRect(Client client, Graphics2D g, LocalPoint p, Rect r, int angle, Color outline, Color fill) {

        float[] coordsZ = new float[]{0, 0, 0, 0};
        int[] cx = new int[4];
        int[] cy = new int[4];

        Perspective.modelToCanvas(
                client,
                client.getTopLevelWorldView(),
                r.coordsX.length,
                p.getX(),
                p.getY(),
                0,
                angle,
                r.coordsX,
                r.coordsY,
                coordsZ,
                cx,
                cy
        );

        Polygon canvasPoly = new Polygon();
        for (int i = 0; i < cx.length; i++) {
            canvasPoly.addPoint(cx[i], cy[i]);
        }
        g.setColor(outline);
        g.draw(canvasPoly);
        g.setColor(fill);
        g.fill(canvasPoly);
    }


    public BoatStep[] generateSteps(int nSteps, int fromOrientation, int toOrientation) {
        // show boat movement preview
        var steps = new BoatStep[nSteps];
        var dA = 128;
        int orientation = fromOrientation;

        var acceleration = boatManager.boatAcceleration;
        var speed = plugin.currentSpeed;

        if (boatManager.isReversing()) {
            speed *= -1;
            acceleration *= -1;
        }

        var dirA = SailingMath.calculateAngleDirectionBetweenOrientations(orientation, toOrientation, plugin.currentTurnDirection);
        var pos = new Point(0, 0);
        for (int i = 0; i < nSteps; i++) {

            // take the max just in case we aren't accounting for something in max speed calculation
            var maxSpeed = Math.max(boatManager.getMaxSpeed(i), plugin.currentSpeed);

            if (orientation != toOrientation) {
                orientation += dA * dirA;
            }

            speed += acceleration;
            speed = !boatManager.isReversing() ? Math.min(maxSpeed, speed) : Math.max(-maxSpeed, speed);

            orientation = SailingMath.cycleOrientation(orientation);

            var angleInDegrees = SailingMath.orientationToDegrees(orientation);
            var v = SailingMath.getVelocity(speed, angleInDegrees);
            pos = new Point(pos.getX() + v.getX(), pos.getY() + v.getY());
            steps[i] = new BoatStep(pos, orientation);
        }

        return steps;
    }

}
