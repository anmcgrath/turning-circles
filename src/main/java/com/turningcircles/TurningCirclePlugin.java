package com.turningcircles;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.GameEventManager;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@PluginDescriptor(
        name = "Turning Circles"
)
public class TurningCirclePlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private TurningCirclesConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private GameEventManager gameEventManager;

    @Inject
    private TurningCirclesOverlay turningCirclesOverlay;

    @Inject
    private BoatStatsOverlay boatStatsOverlay;

    @Inject
    private EventBus eventBus;

    @Inject
    private BoatManager boatManager;

    // track velocity of the boat
    public double currentSpeed;
    public int currentTurnDirection;
    public int lastAngle;
    public double lastSpeed;
    public double currentAcceleration;
    private LocalPoint lastLoc = null;


    @Override
    protected void startUp() throws Exception {
        overlayManager.add(turningCirclesOverlay);
        overlayManager.add(boatStatsOverlay);

        boatManager.startUp();
        eventBus.register(boatManager);
        gameEventManager.simulateGameEvents(boatManager);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(turningCirclesOverlay);
        overlayManager.remove(boatStatsOverlay);

        eventBus.unregister(boatManager);
        boatManager.shutDown();
    }

    @Subscribe
    public void onGameTick(GameTick e) {
        var boatEntity = boatManager.getBoatEntity();
        if (boatEntity == null)
            return;

        var loc = boatEntity.getTargetLocation();
        if (lastLoc != null) {
            var vx = loc.getX() - lastLoc.getX();
            var vy = loc.getY() - lastLoc.getY();

            var speed = SailingMath.getSpeed(vx, vy);
            var accel = (speed - lastSpeed);

            currentTurnDirection = SailingMath.calculateAngleDirectionBetweenOrientations(
                    lastAngle, boatEntity.getTargetOrientation(), 0);

            lastAngle = boatEntity.getTargetOrientation();

            // help protect against moving chunks with large acceleration
            // don't update speed if really big change
            if (accel < 10) {
                currentSpeed = speed;
                lastSpeed = currentSpeed;
                currentAcceleration = accel;
            }

        }

        lastLoc = loc;
    }

    @Provides
    TurningCirclesConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TurningCirclesConfig.class);
    }

    /// Returns the orientation that the user's mouse is pointing towards
    public int calculateMouseHeading(Client client, LocalPoint boatCentre) {

        // This kind of ridiculous, and there must be a better way.
        // But make lines that evenly divide the heading sectors from the centre
        // of the boat outwards in the canvas space.
        // Then check which side of each line the mouse point is to determine
        // which sector we are in.
        int nLines = 16;

        // Boat's centre on canvas
        var centre = Perspective.localToCanvas(client, boatCentre, 0);
        if (centre == null)
            return 0;

        var lineX = new float[]{0, 0};
        var lineY = new float[]{1000, -1000};
        var lineZ = new float[]{0, 0};

        var lines = new Line[nLines];
        for (int n = 0; n < nLines; n++) {
            // start at 64 as we are evenly dividing the sectors
            // south is zero/2048, north is 1024
            int a = 64 + 128 * n;
            int[] cx = new int[2];
            int[] cy = new int[2];
            Perspective.modelToCanvas(
                    client,
                    client.getTopLevelWorldView(),
                    2,
                    boatCentre.getX(), boatCentre.getY(),
                    0,
                    a,
                    lineX,
                    lineY,
                    lineZ,
                    cx,
                    cy
            );
            lines[n] = new Line(centre.getX(), centre.getY(), cx[1], cy[1]);
        }

        var cMouse = client.getMouseCanvasPosition();
        for (int i = 0; i < nLines - 1; i++) {
            var line = lines[i];
            var line1 = lines[i + 1];
            var val0 = line.calculateSide(cMouse.getX(), cMouse.getY());
            var val1 = line1.calculateSide(cMouse.getX(), cMouse.getY());
            if (val0 >= 0 && val1 < 0)
                return 128 + i * 128;
        }

        return 0; // sse
    }
}
