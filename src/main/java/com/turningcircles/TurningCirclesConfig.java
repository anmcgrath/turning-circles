package com.turningcircles;

import net.runelite.client.config.*;
import net.runelite.client.util.ColorUtil;

import java.awt.*;

@ConfigGroup("turningcircles")
public interface TurningCirclesConfig extends Config {
    @ConfigSection(
            name = "Path",
            description = "Settings for the pathing",
            position = 0
    )
    String SECTION_PATH = "path";

    @ConfigSection(
            name = "Styling",
            description = "Settings for the colors used to draw the path",
            position = 10
    )
    String SECTION_STYLING = "styling";

    @ConfigItem(
            keyName = "nSteps",
            name = "Number of steps",
            description = "Number of steps to simulate in the turning circle",
            position = 0,
            section = SECTION_PATH
    )
    @Range(max = 20, min = 1)
    default int nSteps() {
        return 10;
    }

    @ConfigItem(
            keyName = "showWhenStopped",
            name = "Show when stopped",
            description = "Shows the turning circle when the boat is stopped",
            position = 10,
            section = SECTION_PATH
    )
    default boolean showWhenStopped() {
        return true;
    }

    @ConfigItem(
            keyName = "showBoatSpeed",
            name = "Show boat speed/acceleration",
            description = "Show the current boat speed and acceleration",
            position = 20,
            section = SECTION_PATH
    )
    default boolean showBoatSpeed() {
        return false;
    }

    @Alpha
    @ConfigItem(
            keyName = "renderColor",
            name = "Outline color",
            description = "The outline color of the turning circle",
            position = 0,
            section = SECTION_STYLING
    )
    default Color renderColor() {
        return ColorUtil.colorWithAlpha(Color.WHITE, 140);
    }

    @ConfigItem(
            keyName = "fadeOutlineAlpha",
            name = "Fade outline alpha",
            description = "Fades the outline alpha to the setting below",
            position = 10,
            section = SECTION_STYLING
    )
    default boolean fadeOutlineAlpha() {
        return true;
    }

    @ConfigItem(
            keyName = "finalOutlineAlpha",
            name = "Final outline alpha",
            description = "The opacity/alpha of the final outline colour will fade to this if the fade setting is on.",
            position = 20,
            section = SECTION_STYLING
    )
    @Range(max = 255)
    default int finalOutlineAlpha() {
        return 90;
    }

    @Alpha
    @ConfigItem(
            keyName = "fillColor",
            name = "Background color",
            description = "The background color of the turning circle",
            position = 30,
            section = SECTION_STYLING
    )
    default Color fillColor() {
        return ColorUtil.colorWithAlpha(Color.WHITE, 0);
    }

    @ConfigItem(
            keyName = "fadeBackgroundAlpha",
            name = "Fade background alpha",
            description = "Fades the background alpha to the setting below",
            position = 40,
            section = SECTION_STYLING
    )
    default boolean fadeBackgroundAlpha() {
        return false;
    }

    @ConfigItem(
            keyName = "finalBackgroundAlpha",
            name = "Final background alpha",
            description = "The opacity/alpha of the final background colour will fade to this if the fade setting is on.",
            position = 50,
            section = SECTION_STYLING
    )
    @Range(max = 255)
    default int finalBackgroundAlpha() {
        return 0;
    }


}
