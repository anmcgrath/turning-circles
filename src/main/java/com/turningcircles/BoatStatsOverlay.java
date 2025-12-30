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
        if (!boatManager.isOnBoat)
            return null;

        if (!config.showBoatSpeed())
            return null;

        graphics.setColor(Color.YELLOW);
        graphics.drawString("Speed: " + plugin.currentSpeed, 10, 20);
        graphics.drawString("Acceleration: " + plugin.currentAcceleration, 10, 40);
        graphics.setBackground(new Color(0, 0, 0, 20));
        graphics.setStroke(new BasicStroke(1));
        graphics.setColor(Color.WHITE);
        graphics.drawRect(0, 0, 110, 50);

        return new Dimension(100, 50);
    }
}
