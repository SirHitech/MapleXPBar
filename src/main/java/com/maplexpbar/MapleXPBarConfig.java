package com.maplexpbar;

import net.runelite.api.Skill;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("MapleXP")
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

	@ConfigItem(
			position = 1,
			keyName = "mostRecentSkill",
			name = "Show Most Recent Skill",
			description = "Display bar of the most recently skill with xp gain"
	)
	default boolean mostRecentSkill() { return false; }

	@ConfigItem(
			position = 2,
			keyName = "displayHealthAndPrayer",
			name = "Display Health And Prayer",
			description = "Also shows a health and prayer bar"
	)
	default boolean displayHealthAndPrayer() { return false; }

	@ConfigItem(
			position = 3,
			keyName = "showPercentage",
			name = "Show XP Percentage",
			description = "Also shows XP percentage when hovering over bar"
	)
	default boolean showPercentage() { return true; }

	@Alpha
	@ConfigItem(
			position = 4,
			keyName = "hpbarColor",
			name = "HP Bar Color",
			description = "Configures the color of the HP bar"
	)
	default Color colorHP()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
			position = 5,
			keyName = "hpbarNotchColor",
			name = "HP Notch Color",
			description = "Configures the color of the HP bar notches"
	)
	default Color colorHPNotches()
	{
		return Color.LIGHT_GRAY;
	}

	@Alpha
	@ConfigItem(
			position = 6,
			keyName = "praybarColor",
			name = "Prayer Bar Color",
			description = "Configures the color of the Prayer bar"
	)
	default Color colorPray()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
			position = 7,
			keyName = "praybarNotchColor",
			name = "Prayer Notch Color",
			description = "Configures the color of the Prayer bar notches"
	)
	default Color colorPrayNotches()
	{
		return Color.DARK_GRAY;
	}

	@Alpha
	@ConfigItem(
			position = 8,
			keyName = "xpbarColor",
			name = "XP Progress Bar Color",
			description = "Configures the color of the XP bar"
	)
	default Color colorXP()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
			position = 9,
			keyName = "xpbarNotchColor",
			name = "XP Notch Color",
			description = "Configures the color of the XP bar notches"
	)
	default Color colorXPNotches()
	{
		return Color.LIGHT_GRAY;
	}

	@Alpha
	@ConfigItem(
			position = 10,
			keyName = "xpbarSkillColor",
			name = "XP Progressbar as skill color",
			description = "Configure the latest skill color as the XP bar color"
	)
	default boolean mostRecentSkillColor() { return false; }

}
