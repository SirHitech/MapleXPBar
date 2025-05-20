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

	@ConfigSection(
			name = "Health and Prayer Mode",
			description = "Configure the look of the bars when in Health and Prayer mode",
			position = 2
	)
	String healthAndPrayerSection = "healthAndPrayerMode";

	@ConfigSection(
			name = "Multi Skill Mode",
			description = "Configure the look of the bars when in Multi Skill mode",
			position = 3
	)
	String multiSkillModeSection = "multiSkillMode";

	@ConfigSection(
			name = "Bar Position and Sizing",
			description = "Options to reposition and resize the experience bar",
			position = 4
	)
	String positionSizingSection = "positionAndSizing";

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
			position = 0,
			keyName = "alwaysShowTooltip",
			name = "Always Display Tooltip",
			section = advancedSection,
			description = "Always display the progress tooltip"
	)
	default boolean alwaysShowTooltip() { return false; }

	@ConfigItem(
			position = 1,
			keyName = "tooltipMode",
			name = "Tooltip Text",
			section = advancedSection,
			description = "Display the current over total XP, XP as a percent to next level, or both"
	)
	default MapleXPBarTooltipMode tooltipMode() { return MapleXPBarTooltipMode.BOTH; }

	@Alpha
	@ConfigItem(
			position = 2,
			keyName = "xpbarColor",
			name = "XP Progress Color",
			section = advancedSection,
			description = "Configures the progress color of the XP bar"
	)
	default Color colorXP()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
			position = 3,
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
			position = 4,
			keyName = "xpbarBackgroundColor",
			name = "XP Bar Background",
			section = advancedSection,
			description = "Configures the background color of the XP bar"
	)
	default Color colorXPBackground()
	{
		return Color.BLACK;
	}

	@Alpha
	@ConfigItem(
			position = 5,
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
			position = 6,
			keyName = "xpbarSkillColor",
			name = "Automatically Pick Skill Color",
			section = advancedSection,
			description = "Use the skill's color instead of the user set color"
	)
	default boolean shouldAutoPickSkillColor() { return false; }

	@ConfigItem(
			position = 7,
			keyName = "barMode",
			name = "Bar Mode",
			section = advancedSection,
			description = "Single: only displays the XP bar<br>Health and Prayer: display current HP and Pray above the XP bar<br>Multi Skill: Display XP bars for 3 skills at once"
	)
	default MapleXPBarMode barMode() { return MapleXPBarMode.SINGLE; }

	@Alpha
	@ConfigItem(
			position = 0,
			keyName = "hpbarColor",
			name = "HP Bar Color",
			section = healthAndPrayerSection,
			description = "Configures the color of the HP bar"
	)
	default Color colorHP()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
			position = 1,
			keyName = "hpbarNotchColor",
			name = "HP Notch Color",
			section = healthAndPrayerSection,
			description = "Configures the color of the HP bar notches"
	)
	default Color colorHPNotches()
	{
		return Color.LIGHT_GRAY;
	}

	@Alpha
	@ConfigItem(
			position = 2,
			keyName = "hpbarBackgroundColor",
			name = "HP Bar Background",
			section = healthAndPrayerSection,
			description = "Configures the background color of the HP bar"
	)
	default Color colorHPBackground()
	{
		return Color.BLACK;
	}

	@Alpha
	@ConfigItem(
			position = 3,
			keyName = "praybarColor",
			name = "Prayer Bar Color",
			section = healthAndPrayerSection,
			description = "Configures the color of the Prayer bar"
	)
	default Color colorPray()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
			position = 4,
			keyName = "praybarNotchColor",
			name = "Prayer Notch Color",
			section = healthAndPrayerSection,
			description = "Configures the color of the Prayer bar notches"
	)
	default Color colorPrayNotches()
	{
		return Color.DARK_GRAY;
	}

	@Alpha
	@ConfigItem(
			position = 5,
			keyName = "praybarBackgroundColor",
			name = "Prayer Bar Background",
			section = healthAndPrayerSection,
			description = "Configures the background color of the Prayer bar"
	)
	default Color colorPrayBackground()
	{
		return Color.BLACK;
	}

	@ConfigItem(
			position = 0,
			keyName = "skill2",
			name = "Skill 2",
			section = multiSkillModeSection,
			description = "Choose which skill to show xp for on the second bar"
	)
	default Skill skill2()
	{
		return Skill.STRENGTH;
	}

	@Alpha
	@ConfigItem(
			position = 1,
			keyName = "xpbarSkill2Color",
			name = "Automatically Pick Skill 2 Color",
			section = multiSkillModeSection,
			description = "Use the skill's color instead of the user set color"
	)
	default boolean shouldAutoPickSkill2Color() { return true; }

	@Alpha
	@ConfigItem(
			position = 2,
			keyName = "skill2barColor",
			name = "Skill 2 Progress Color",
			section = multiSkillModeSection,
			description = "Configures the color of the second skill bar"
	)
	default Color colorSkill2()
	{
		return Color.ORANGE;
	}

	@Alpha
	@ConfigItem(
			position = 3,
			keyName = "skill2barNotchColor",
			name = "Skill 2 Notch Color",
			section = multiSkillModeSection,
			description = "Configures the color of the second skill bar notches"
	)
	default Color colorSkill2Notches()
	{
		return Color.DARK_GRAY;
	}

	@Alpha
	@ConfigItem(
			position = 4,
			keyName = "skill2barBackgroundColor",
			name = "Skill 2 Background",
			section = multiSkillModeSection,
			description = "Configures the color of the second skill bar background"
	)
	default Color colorSkill2Background()
	{
		return Color.BLACK;
	}

	@ConfigItem(
			position = 5,
			keyName = "skill3",
			name = "Skill 3",
			section = multiSkillModeSection,
			description = "Choose which skill to show xp for on the third bar"
	)
	default Skill skill3()
	{
		return Skill.DEFENCE;
	}

	@Alpha
	@ConfigItem(
			position = 6,
			keyName = "xpbarSkill3Color",
			name = "Automatically Pick Skill 3 Color",
			section = multiSkillModeSection,
			description = "Use the skill's color instead of the user set color"
	)
	default boolean shouldAutoPickSkill3Color() { return true; }

	@Alpha
	@ConfigItem(
			position = 7,
			keyName = "skill3barColor",
			name = "Skill 3 Progress Color",
			section = multiSkillModeSection,
			description = "Configures the color of the third skill bar"
	)
	default Color colorSkill3()
	{
		return Color.PINK;
	}

	@Alpha
	@ConfigItem(
			position = 8,
			keyName = "skill3barBackgroundColor",
			name = "Skill 3 Background",
			section = multiSkillModeSection,
			description = "Configures the color of the third skill bar background"
	)
	default Color colorSkill3Background()
	{
		return Color.BLACK;
	}

	@Alpha
	@ConfigItem(
			position = 8,
			keyName = "skill3barNotchColor",
			name = "Skill 3 Notch Color",
			section = multiSkillModeSection,
			description = "Configures the color of the third skill bar notches"
	)
	default Color colorSkill3Notches()
	{
		return Color.DARK_GRAY;
	}

	@ConfigItem(
			position = 0,
			keyName = "anchorToChatbox",
			name = "Anchor to Chatbox",
			section = positionSizingSection,
			description = "When enabled, the offset values are in reference to top of the chatbox. When off, they are in reference to the top-left of the client"
	)
	default boolean anchorToChatbox() { return true; }

	@Range(min=-9999, max=9999)
	@ConfigItem(
			position = 1,
			keyName = "manualXOffset",
			name = "Offset Left/Right",
			section = positionSizingSection,
			description = "Offset the position of the XP bar horizontally. A higher number moves the UI to the right"
	)
	default int manualOffsetX() { return 0; }

	@Range(min=-9999, max=9999)
	@ConfigItem(
			position = 2,
			keyName = "manualYOffset",
			name = "Offset Up/Down",
			section = positionSizingSection,
			description = "Offset the position of the XP bar vertically. A higher number moves the UI up"
	)
	default int manualOffsetY() { return 0; }

	@Range(min=3, max=50)
	@ConfigItem(
			position = 3,
			keyName = "xpbarThickness",
			name = "Bar Thickness",
			section = positionSizingSection,
			description = "Adjust the thickness of the XP bar"
	)
	default int thickness() { return 4; }

	@Range(min=1, max=10000)
	@ConfigItem(
			position = 4,
			keyName = "xpbarLength",
			name = "Bar Length",
			section = positionSizingSection,
			description = "Adjust the length of the XP bar"
	)
	default int length() { return 512; }

	@Range(min=1, max=100)
	@ConfigItem(
			position = 5,
			keyName = "xpTextSize",
			name = "Font Size",
			section = positionSizingSection,
			description = "Adjust the font size for the xp progress text"
	)
	default int fontSize() { return 16; }

}
