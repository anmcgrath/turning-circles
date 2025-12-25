package com.turningcircles;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

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

        var boat = boatManager.getBoatEntity();
        if (boat == null) return null;

        var bc = boat.getConfig();
        var boatRect = new Rect(bc.getBoundsX(), bc.getBoundsY(), bc.getBoundsWidth(), bc.getBoundsHeight());

        var mouseHeading = plugin.calculateMouseHeading(client, boat.getLocalLocation());

        if (plugin.currentSpeed == 0)
            return null;

        var paths = SailingMath.generateSteps(
                config.nSteps(),
                boat.getTargetOrientation(),
                mouseHeading,
                boat.getTargetLocation(),
                plugin.currentSpeed,
                plugin.currentTurnDirection);

        g.setColor(config.renderColor());

        for (var path : paths) {
            renderRotatedRect(client, g, path.location, boatRect, path.orientation);
        }

        return null;
    }

    private void renderRotatedRect(Client client, Graphics2D g, LocalPoint p, Rect r, int angle) {

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
        g.draw(canvasPoly);
    }

}
