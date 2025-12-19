package com.turningcircles;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TurningCirclesPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TurningCirclePlugin.class);
		RuneLite.main(args);
	}
}