package com.turningcircles;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

import java.awt.*;

@ConfigGroup("turningcircles")
public interface TurningCirclesConfig extends Config {
    @ConfigItem(
            keyName = "nSteps",
            name = "Number of steps",
            description = "Number of steps to simulate in the turning circle",
            position = 0
    )
    @Range(max = 20, min = 1)
    default int nSteps() {
        return 10;
    }

    @ConfigItem(
            keyName = "renderColor",
            name = "Render color",
            description = "The color of the turning circle",
            position = 1
    )
    default Color renderColor() {
        return Color.WHITE;
    }

    @ConfigItem(
            keyName = "hideWhenStopped",
            name = "Hide when stopped",
            description = "Hides the turning circle when the boat is stopped",
            position = 2
    )
    default boolean hideWhenStopped() {
        return true;
    }
}
