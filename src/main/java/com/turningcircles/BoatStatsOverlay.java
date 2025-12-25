package com.turningcircles;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import java.awt.*;

public class BoatStatsOverlay extends Overlay {

    private final Client client;
    private final TurningCirclePlugin plugin;
    private final TurningCirclesConfig config;
    private final BoatManager boatManager;

    @Inject
    public BoatStatsOverlay(Client client, TurningCirclePlugin plugin, TurningCirclesConfig config, BoatManager boatManager) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.boatManager = boatManager;
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.TOP_CENTER);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        graphics.drawString("Speed: " + plugin.currentSpeed, 0, 20);
        graphics.drawString("Acceleration: " + plugin.currentAcceleration, 0, 40);
        graphics.drawString("Heading: " + plugin.currentTurnDirection, 0, 60);
        graphics.drawString("Ticks since mote: " + boatManager.ticksSinceMote, 0, 80);
        graphics.drawString("Has crystal mote boost: " + boatManager.isCrystalMoteSpeedBoostActive(), 0, 100);
        return null;
    }
}
