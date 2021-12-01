package com.maplexpbar;

import net.runelite.api.Skill;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.prayer.PrayerFlickLocation;

import java.awt.*;

@ConfigGroup("example")
public interface MapleXPBarConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "skill",
			name = "Progress Bar Skill",
			description = "Choose which skill to show xp for"
	)
	default Skill skill()
	{
		return Skill.ATTACK;
	}

	@Alpha
	@ConfigItem(
			keyName = "barColor",
			name = "Progress Bar Color",
			description = "Configures the color of the XP bar"
	)
	default Color color()
	{
		return Color.GREEN;
	}
}
