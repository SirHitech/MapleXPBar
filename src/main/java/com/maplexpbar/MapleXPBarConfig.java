package com.maplexpbar;

import net.runelite.api.Skill;
import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("MapleXP")
public interface MapleXPBarConfig extends Config
{
	@ConfigSection(
			name = "Configuration",
			description = "General configuration options",
			position = 0
	)
	String generalSection = "general";

	@ConfigSection(
			name = "Advanced Display Options",
			description = "Advanced display options for further customization",
			position = 1
	)
	String advancedSection = "advanced";

	@ConfigItem(
			position = 0,
			keyName = "skill",
			name = "Progress Bar Skill",
			section = generalSection,
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
			section = generalSection,
			description = "Display bar of the most recent skill with xp gain"
	)
	default boolean mostRecentSkill() { return false; }

	@ConfigItem(
			position = 2,
			keyName = "ignoreRecentHitpoints",
			name = "Ignore Hitpoints as Recent",
			section = generalSection,
			description = "Ignore hitpoints if showing bar as most recent skill"
	)
	default boolean ignoreRecentHitpoints() { return true; }

	@ConfigItem(
			position = 3,
			keyName = "showMaxedSkills",
			name = "Show Maxed Skills",
			section = generalSection,
			description = "Display bar even if the skill XP exceeds threshold"
	)
	default boolean showMaxedSkills() { return false; }

	@Range(max=200000000)
	@ConfigItem(
			position = 4,
			keyName = "maxedThreshold",
			name = "Maxed XP Threshold",
			section = generalSection,
			description = "If Show Maxed Skills is checked, use this threshold for XP for considering a skill 'maxed'"
	)
	default int maxedThreshold() { return 13034431; }

	@ConfigItem(
			position = 5,
			keyName = "displayHealthAndPrayer",
			name = "Display Health And Prayer",
			section = generalSection,
			description = "Also shows a health and prayer bar"
	)
	default boolean displayHealthAndPrayer() { return false; }

	@ConfigItem(
			position = 0,
			keyName = "alwaysShowTooltip",
			name = "Always Display Tooltip",
			section = advancedSection,
			description = "Always display the progress tooltip"
	)
	default boolean alwaysShowTooltip() { return false; }

	@ConfigItem(
			position = 1,
			keyName = "showPercentage",
			name = "Show XP Percentage",
			section = advancedSection,
			description = "Also shows XP percentage when hovering over bar"
	)
	default boolean showPercentage() { return true; }

	@ConfigItem(
			position = 2,
			keyName = "showOnlyPercentage",
			name = "Only show Percentage",
			section = advancedSection,
			description = "When showing percentage, hide the current XP/next level XP"
	)
	default boolean showOnlyPercentage() { return false; }

	@Alpha
	@ConfigItem(
			position = 3,
			keyName = "hpbarColor",
			name = "HP Bar Color",
			section = advancedSection,
			description = "Configures the color of the HP bar"
	)
	default Color colorHP()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
			position = 4,
			keyName = "hpbarNotchColor",
			name = "HP Notch Color",
			section = advancedSection,
			description = "Configures the color of the HP bar notches"
	)
	default Color colorHPNotches()
	{
		return Color.LIGHT_GRAY;
	}

	@Alpha
	@ConfigItem(
			position = 5,
			keyName = "praybarColor",
			name = "Prayer Bar Color",
			section = advancedSection,
			description = "Configures the color of the Prayer bar"
	)
	default Color colorPray()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
			position = 6,
			keyName = "praybarNotchColor",
			name = "Prayer Notch Color",
			section = advancedSection,
			description = "Configures the color of the Prayer bar notches"
	)
	default Color colorPrayNotches()
	{
		return Color.DARK_GRAY;
	}

	@Alpha
	@ConfigItem(
			position = 7,
			keyName = "xpbarColor",
			name = "XP Progress Bar Color",
			section = advancedSection,
			description = "Configures the color of the XP bar"
	)
	default Color colorXP()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
			position = 8,
			keyName = "xpbarNotchColor",
			name = "XP Notch Color",
			section = advancedSection,
			description = "Configures the color of the XP bar notches"
	)
	default Color colorXPNotches()
	{
		return Color.LIGHT_GRAY;
	}

	@Alpha
	@ConfigItem(
			position = 9,
			keyName = "xpbarTextColor",
			name = "XP Text Color",
			section = advancedSection,
			description = "Configures the color of the progress text above the XP bar"
	)
	default Color colorXPText()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(
			position = 10,
			keyName = "xpbarSkillColor",
			name = "XP Progressbar as skill color",
			section = advancedSection,
			description = "Configure the latest skill color as the XP bar color"
	)
	default boolean mostRecentSkillColor() { return false; }

	@Range(min=-9999, max=9999)
	@ConfigItem(
			position = 11,
			keyName = "manualXOffset",
			name = "Offset Left/Right",
			section = advancedSection,
			description = "Offset the position of the XP bar horizontally. A higher number moves the UI to the right"
	)
	default int manualOffsetX() { return 0; }

	@Range(min=-9999, max=9999)
	@ConfigItem(
			position = 12,
			keyName = "manualYOffset",
			name = "Offset Up/Down",
			section = advancedSection,
			description = "Offset the position of the XP bar vertically. A higher number moves the UI down"
	)
	default int manualOffsetY() { return 0; }

	@Range(min=1, max=1000)
	@ConfigItem(
		position = 13,
		keyName = "xpbarScaling",
		name = "Size (as %)",
		section = advancedSection,
		description = "Resize the XP Bar"
	)
	default int scalingModifier() { return 100; }

}
