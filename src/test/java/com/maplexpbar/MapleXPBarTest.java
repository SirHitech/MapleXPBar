package com.maplexpbar;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MapleXPBarTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MapleXPBarPlugin.class);
		RuneLite.main(args);
	}
}