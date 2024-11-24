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
			description = "Display bar of the most recent skill with xp gain"
	)
	default boolean mostRecentSkill() { return false; }

	@ConfigItem(
			position = 2,
			keyName = "ignoreRecentHitpoints",
			name = "Ignore Hitpoints as Recent",
			description = "Ignore hitpoints if showing bar as most recent skill"
	)
	default boolean ignoreRecentHitpoints() { return true; }

	@ConfigItem(
			position = 3,
			keyName = "showMaxedSkills",
			name = "Show Maxed Skills",
			description = "Display bar even if the skill XP exceeds threshold"
	)
	default boolean showMaxedSkills() { return false; }

	@ConfigItem(
			position = 4,
			keyName = "maxedThreshold",
			name = "Maxed XP Threshold",
			description = "If Show Maxed Skills is checked, use this threshold for XP for considering a skill 'maxed'"
	)
	default int maxedThreshold() { return 13034431; }

	@ConfigItem(
			position = 5,
			keyName = "displayHealthAndPrayer",
			name = "Display Health And Prayer",
			description = "Also shows a health and prayer bar"
	)
	default boolean displayHealthAndPrayer() { return false; }

	@ConfigItem(
			position = 6,
			keyName = "alwaysShowTooltip",
			name = "Always Display Tooltip",
			description = "Always display the progress tooltip"
	)
	default boolean alwaysShowTooltip() { return false; }

	@ConfigItem(
			position = 7,
			keyName = "showPercentage",
			name = "Show XP Percentage",
			description = "Also shows XP percentage when hovering over bar"
	)
	default boolean showPercentage() { return true; }

	@ConfigItem(
			position = 8,
			keyName = "showOnlyPercentage",
			name = "Only show Percentage",
			description = "When showing percentage, hide the current XP/next level XP"
	)
	default boolean showOnlyPercentage() { return false; }

	@Alpha
	@ConfigItem(
			position = 9,
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
			position = 10,
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
			position = 11,
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
			position = 12,
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
			position = 13,
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
			position = 14,
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
			position = 15,
			keyName = "xpbarSkillColor",
			name = "XP Progressbar as skill color",
			description = "Configure the latest skill color as the XP bar color"
	)
	default boolean mostRecentSkillColor() { return false; }

}
