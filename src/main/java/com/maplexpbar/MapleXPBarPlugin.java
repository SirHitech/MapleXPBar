package com.maplexpbar;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Varbits;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.SkillColor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;


import net.runelite.client.ui.overlay.OverlayPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

@Slf4j
@PluginDescriptor(
	name = "Maple XP Bar"
)
public class MapleXPBarPlugin extends Plugin
{
	@Inject
	private XPBarOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	public Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MapleXPBarConfig config;

	@Getter(AccessLevel.PACKAGE)
	private boolean barsDisplayed;

	@Getter(AccessLevel.PACKAGE)
	private Skill currentSkill;

	@Getter(AccessLevel.PACKAGE)
	private Font font;

	private final Map<Skill, Integer> skillList = new EnumMap<>(Skill.class);

	@Override
	protected void startUp()
	{
		font = FontManager.getRunescapeSmallFont().deriveFont((float)config.fontSize());
		overlayManager.add(overlay);
		migrate();
		barsDisplayed = true;
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		barsDisplayed = false;
	}

	@Provides
	MapleXPBarConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MapleXPBarConfig.class);
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged) {

		if (statChanged.getSkill() == Skill.HITPOINTS && config.ignoreRecentHitpoints())
		{
			return;
		}

		Integer lastXP = skillList.put(statChanged.getSkill(), statChanged.getXp());

		if (lastXP != null && lastXP != statChanged.getXp()) {
			Integer xpThreshold = config.maxedThreshold();
			boolean exceedsThreshold = lastXP >= xpThreshold;

			if (! exceedsThreshold || config.showMaxedSkills())
			{
				currentSkill = statChanged.getSkill();
			}
		}

		log.debug("State CHANGED: " + statChanged.getSkill());
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if(event.getGroup().equals("MapleXP")
				&& event.getKey().equals("xpTextSize")
				&& event.getNewValue() != null)
		{
			font = font.deriveFont(Float.parseFloat(event.getNewValue()));
		}
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged profileChanged)
	{
		migrate();
	}

	private void migrate()
	{
		// old HP/Pray bar config migration
		Boolean oldDisplayHealthAndPrayer = configManager.getConfiguration("MapleXP", "displayHealthAndPrayer", Boolean.class);
		if (oldDisplayHealthAndPrayer != null)
		{
			if (oldDisplayHealthAndPrayer){
				// convert legacy setting to new one
				configManager.setConfiguration("MapleXP", "barMode", MapleXPBarMode.HEALTH_AND_PRAYER);
			}
			configManager.unsetConfiguration("MapleXP", "displayHealthAndPrayer");
		}

		// old tooltip configs migration
		Boolean oldShowPercentage = configManager.getConfiguration("MapleXP", "showPercentage", Boolean.class);
		Boolean oldShowOnlyPercentage = configManager.getConfiguration("MapleXP", "showOnlyPercentage", Boolean.class);
		if (oldShowPercentage != null && oldShowOnlyPercentage != null)
		{
			MapleXPBarTooltipMode mode;
			if (oldShowPercentage){
				// convert legacy setting to new one
				configManager.setConfiguration("MapleXP", "tooltipMode", oldShowOnlyPercentage ? MapleXPBarTooltipMode.PERCENTAGE : MapleXPBarTooltipMode.BOTH);
			}
			else
			{
				configManager.setConfiguration("MapleXP", "tooltipMode", MapleXPBarTooltipMode.CURRENT_XP);
			}
			configManager.unsetConfiguration("MapleXP", "showPercentage");
			configManager.unsetConfiguration("MapleXP", "showOnlyPercentage");
		}

		// old anchor to chatbox migration
		Boolean oldAnchorToChatbox = configManager.getConfiguration("MapleXP", "anchorToChatbox", Boolean.class);
		if (oldAnchorToChatbox != null){
			// convert legacy setting to new one
			configManager.setConfiguration("MapleXP", "anchorPoint", oldAnchorToChatbox ? MapleXPBarAnchorMode.CHATBOX : MapleXPBarAnchorMode.TOP_LEFT);
		}
		configManager.unsetConfiguration("MapleXP", "anchorToChatbox");
	}
}

@Slf4j
class XPBarOverlay extends Overlay
{
	private MapleXPBarConfig config;
	private Client client;
	private static final Logger logger = LoggerFactory.getLogger(XPBarOverlay.class);
	static int HEIGHT = 4;
	private static final int BORDER_SIZE = 1;

	private final MapleXPBarPlugin plugin;
	private final SkillIconManager skillIconManager;

	@Inject
	private XPBarOverlay(Client client, MapleXPBarPlugin plugin, MapleXPBarConfig config, SkillIconManager skillIconManager, SpriteManager spriteManager)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.skillIconManager = skillIconManager;
	}

	/**
	 * Entry point render method that handles when the bar should be rendered,
	 * and which viewport and anchor point to offset from. Delegates the rendering
	 * to {@link #renderBar(Graphics2D, MapleXPBarMode, int, int)}.
	 * @param g		Graphics2D object to help render our 2D UI
	 * @return		null
	 */
	@Override
	public Dimension render(Graphics2D g)
	{
		if (!plugin.isBarsDisplayed())
		{
			return null;
		}

		// Hide bar when there are no recent skills, in most recent skill mode.
		if (config.mostRecentSkill() && plugin.getCurrentSkill() == null)
		{
			return null;
		}

		Point offset = null;
		Point chatboxAnchorLocation = null;

		// Determine where to draw the bar, using the anchor point config selected
		for (Viewport viewport : Viewport.values())
		{
			final Widget viewportWidget = client.getWidget(viewport.getViewport());
			if (viewportWidget != null && !viewportWidget.isHidden() && viewport.getType().equals(config.anchorPoint().getMenuName()))
			{
				offset = viewport.getOffsetLeft();
				chatboxAnchorLocation = viewportWidget.getCanvasLocation();
				break;
			}
		}

		if (chatboxAnchorLocation == null)
		{
			// Chatbox or inventory might be hidden - check if a fallback is visible, and use those if applicable
			for (FallbackViewport viewport : FallbackViewport.values())
			{
				final Widget viewportWidget = client.getWidget(viewport.getViewport());
				if (viewportWidget != null && !viewportWidget.isHidden() && viewport.getType().equals(config.anchorPoint().getMenuName()))
				{
					offset = viewport.getOffsetLeft();
					chatboxAnchorLocation = viewportWidget.getCanvasLocation();
					break;
				}
			}

			if (chatboxAnchorLocation == null){
				// backup component not found, don't render the bar
				return null;
			}
		}

		final int height, offsetBarX, offsetBarY;

		height = config.thickness();
		offsetBarX = chatboxAnchorLocation.getX() - offset.getX();
		offsetBarY = chatboxAnchorLocation.getY() - offset.getY();

		renderBar(g, config.barMode(), offsetBarX, offsetBarY);

		return null;
	}

	/**
	 * Calculate the tooltip text to be displayed based on the tooltip mode config
	 * and the player's XP in the skill.
	 * @param currentXP			The player's total XP in the skill
	 * @param currentLevelXP	The total XP required for the player's current skill level
	 * @param nextLevelXP		The total XP required for the next level
	 * @return The formatted tooltip text
	 */
	private String getTooltipText(int currentXP, int currentLevelXP, int nextLevelXP)
	{
		// Format tooltip display
		DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
		String xpText = df.format(currentXP) + "/" + df.format(nextLevelXP);
		Double percentage = 100.0 * (currentXP - currentLevelXP) / (nextLevelXP - currentLevelXP);

		switch (config.tooltipMode()){
			case CURRENT_XP:
				// xpText is already formatted and needs no further modifying
				break;
			case PERCENTAGE:
				df.applyPattern("0.000");
				xpText = df.format(percentage) + "%";
				break;
			case BOTH:
				df.applyPattern("0.000");
				xpText += " (" + df.format(percentage) + "%)";
				break;
		}

		return xpText;
	}

	/**
	 * Calculates remaining configuration to render the bar and
	 * all of its subcomponents (tooltip, skill icon) to the UI.
	 * @param graphics		Graphics2D for rendering our 2D UI
	 * @param mode			The Bar Mode config selected by the user
	 * @param x				The x position to start drawing the bar
	 * @param y				The y position to start drawing the bar
	 */
	public void renderBar(Graphics2D graphics, MapleXPBarMode mode, int x, int y)
	{
		// Get info for experience
		Skill skill = config.mostRecentSkill() ? plugin.getCurrentSkill() : config.skill();
		int currentXP = client.getSkillExperience(skill);
		int currentLevel = Experience.getLevelForXp(currentXP);
		int nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
		int currentLevelXP = Experience.getXpForLevel(currentLevel);

		// Get info for hp and pray
		int currentHP = client.getBoostedSkillLevel(Skill.HITPOINTS);
		int maxHP = client.getRealSkillLevel(Skill.HITPOINTS);
		int currentPray = client.getBoostedSkillLevel(Skill.PRAYER);
		int maxPray = client.getRealSkillLevel(Skill.PRAYER);

		// Calc starting position for bar
		int adjustedX = x;
		int adjustedY;
		int adjustedWidth = config.length();

		int height = config.thickness();

		// Y Offsets are flipped for the user's sake,
		// so Y config values become: higher number -> UI moves up
		int barOffsetX = config.manualOffsetX();
		int barOffsetY = -1 * config.manualOffsetY();

		int tooltipOffsetX = config.tooltipOffsetX();
		int tooltipOffsetY = -1 * config.tooltipOffsetY();

		boolean isTransparentChatbox = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;
		boolean shouldDisplaySkillIcon = config.shouldDisplaySkillIcon();

		// In-game resizable mode needs some slight adjustment for default values
		if (client.isResized()){
			adjustedX = x - 4;
			adjustedWidth = config.length() + 7;
		}

		// Transparent chatbox looks smaller - adjust if shown
		int[] ALL_CHATBOX_BUTTON_IDS = {10616837, 10616840, 10616844, 10616848, 10616852, 10616856, 10616860};
		boolean isChatShown = false;
		for (int id : ALL_CHATBOX_BUTTON_IDS)
		{
			Integer[] BUTTON_ENABLED_IDS = {3053, 3054};

			if (Arrays.asList(BUTTON_ENABLED_IDS).contains(client.getWidget(id).getSpriteId()))
			{
				isChatShown = true;
				break;
			}
		}

		boolean automaticallyOffsetBar = config.anchorPoint() == MapleXPBarAnchorMode.CHATBOX;

		adjustedY = client.isResized() && isTransparentChatbox && isChatShown && automaticallyOffsetBar? y + 7: y;

		adjustedX += barOffsetX;
		adjustedY += barOffsetY;

		final int filledWidthXP = getBarFillLength(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedWidth);
		final int filledWidthHP = getBarFillLength(maxHP, currentHP, adjustedWidth);
		final int filledWidthPray = getBarFillLength(maxPray, currentPray, adjustedWidth);

		Color barColor;

		if (config.shouldAutoPickSkillColor())
		{
			if (config.mostRecentSkill())
			{
				// As long as there is a recent skill, find it. Otherwise, stop rendering the bar
				if (plugin.getCurrentSkill() == null) return;
				barColor = SkillColor.find(plugin.getCurrentSkill()).getColor();
			}
			else
			{
				barColor = SkillColor.find(config.skill()).getColor();
			}
		}
		else
		{
			barColor = config.colorXP();
		}

		// Most configuration handling is done, start drawing the bar(s)
		drawBar(graphics, adjustedX, adjustedY, adjustedWidth, filledWidthXP, barColor, config.colorXPNotches(), config.colorXPBackground());

		if (mode.equals(MapleXPBarMode.HEALTH_AND_PRAYER)){
			drawBar(graphics, adjustedX, adjustedY - height, adjustedWidth, filledWidthPray, config.colorPray(), config.colorPrayNotches(), config.colorPrayBackground());
			drawBar(graphics, adjustedX, adjustedY - (height * 2), adjustedWidth, filledWidthHP, config.colorHP(), config.colorHPNotches(), config.colorHPBackground());
		}
		else if (mode.equals(MapleXPBarMode.MULTI_SKILL))
		{
			int currentXP2 = client.getSkillExperience(config.skill2());
			int currentLevel2 = Experience.getLevelForXp(currentXP2);
			int nextLevelXP2 = Experience.getXpForLevel(currentLevel2 + 1);
			int currentLevelXP2 = Experience.getXpForLevel(currentLevel2);
			int filledWidthXP2 = getBarFillLength(nextLevelXP2 - currentLevelXP2, currentXP2 - currentLevelXP2, adjustedWidth);
			Color bar2Color = config.shouldAutoPickSkill2Color() ? SkillColor.find(config.skill2()).getColor() : config.colorSkill2();

			int currentXP3 = client.getSkillExperience(config.skill3());
			int currentLevel3 = Experience.getLevelForXp(currentXP3);
			int nextLevelXP3 = Experience.getXpForLevel(currentLevel3 + 1);
			int currentLevelXP3 = Experience.getXpForLevel(currentLevel3);
			int filledWidthXP3 = getBarFillLength(nextLevelXP3 - currentLevelXP3, currentXP3 - currentLevelXP3, adjustedWidth);
			Color bar3Color = config.shouldAutoPickSkill3Color() ? SkillColor.find(config.skill3()).getColor() : config.colorSkill3();

			drawBar(graphics, adjustedX, adjustedY - height, adjustedWidth, filledWidthXP2, bar2Color, config.colorSkill2Notches(), config.colorSkill2Background());
			drawBar(graphics, adjustedX, adjustedY - (height * 2), adjustedWidth, filledWidthXP3, bar3Color, config.colorSkill3Notches(), config.colorSkill3Background());

			String tooltip = "";
			BufferedImage img = shouldDisplaySkillIcon ? skillIconManager.getSkillImage(skill, true) : null;
			boolean	hoveringBar2 = client.getMouseCanvasPosition().getX() >= adjustedX && client.getMouseCanvasPosition().getY() > adjustedY - height
					&& client.getMouseCanvasPosition().getX() <= adjustedX + adjustedWidth && client.getMouseCanvasPosition().getY() <= adjustedY;
			if (hoveringBar2) {
				tooltip = getTooltipText(currentXP2, currentLevelXP2, nextLevelXP2);
				img = shouldDisplaySkillIcon ? skillIconManager.getSkillImage(config.skill2(), true) : null;
			}
			boolean	hoveringBar3 = client.getMouseCanvasPosition().getX() >= adjustedX && client.getMouseCanvasPosition().getY() > adjustedY - (height * 2)
					&& client.getMouseCanvasPosition().getX() <= adjustedX + adjustedWidth && client.getMouseCanvasPosition().getY() <= adjustedY - height;
			if (hoveringBar3) {
				tooltip = getTooltipText(currentXP3, currentLevelXP3, nextLevelXP3);
				img = shouldDisplaySkillIcon ? skillIconManager.getSkillImage(config.skill3(), true) : null;
			}

			// if we're always showing tooltip text for bar 1, we can't show tooltips for either of the other bars
			if (!config.alwaysShowTooltip() && (hoveringBar2 || hoveringBar3)) {
				drawTooltip(graphics, tooltip, adjustedX, adjustedY, tooltipOffsetX, tooltipOffsetY, adjustedWidth, height, !mode.equals(MapleXPBarMode.SINGLE), img);
			}
		}

		String xpText = getTooltipText(currentXP, currentLevelXP, nextLevelXP);

		boolean	hoveringBar = client.getMouseCanvasPosition().getX() >= adjustedX && client.getMouseCanvasPosition().getY() > adjustedY
				&& client.getMouseCanvasPosition().getX() <= adjustedX + adjustedWidth && client.getMouseCanvasPosition().getY() <= adjustedY + height;

		if (hoveringBar || config.alwaysShowTooltip()) {
			BufferedImage img = shouldDisplaySkillIcon ? skillIconManager.getSkillImage(skill, true) : null;
			drawTooltip(graphics, xpText, adjustedX, adjustedY, tooltipOffsetX, tooltipOffsetY, adjustedWidth, height, !mode.equals(MapleXPBarMode.SINGLE), img);
		}
	}

	/**
	 * Helper to draw the tooltip text and skill icon (if passed) at the correct location
	 * @param graphics			Graphics2D to help draw our UI
	 * @param tooltipText		The text to be rendered
	 * @param x					The starting x location to render the tooltip at
	 * @param y					The starting y location to render the bar at
	 * @param offsetX			The amount to vertically offset the tooltip and subcomponent skill icon
	 * @param offsetY			The amount to horizontally offset the tooltip and subcomponent skill icon
	 * @param barWidth			The width of the XP bar, used to calculate tooltip positioning
	 * @param barHeight			The height of each XP bar, used to calculate tooltip positioning
	 * @param isThreeBarMode	If a config option for 3 Bar mode is enabled (either 3 Skill or HP+Pray)
	 * @param skillImage		A BuggeredImage of the skill icon to be drawn, null if the config setting is disabled
	 */
	private void drawTooltip(Graphics2D graphics, String tooltipText, int x, int y, int offsetX, int offsetY, int barWidth, int barHeight, boolean isThreeBarMode, BufferedImage skillImage)
	{
		FontMetrics metrics = graphics.getFontMetrics(FontManager.getRunescapeSmallFont());

		int threeBarOffset = isThreeBarMode ? barHeight * 2 : 0;

		// (stringWidth / 2) keeps the tooltip text middle-aligned
		int tooltipX = x + (barWidth/2 + 8) - (metrics.stringWidth(tooltipText) / 2) + offsetX;
		int tooltipY = y - threeBarOffset + offsetY;

		graphics.setColor(config.colorXPText());
		graphics.setFont(plugin.getFont());
		graphics.drawString(tooltipText, tooltipX, tooltipY);

		if (skillImage != null)
		{
			int iconOffsetX = (-1 * skillImage.getWidth()) + config.iconOffsetX();
			int iconOffsetY = (-1 * skillImage.getHeight()) - config.iconOffsetY();

			graphics.drawImage(skillImage, tooltipX + iconOffsetX, tooltipY + iconOffsetY, null);
		}
	}

	/**
	 * Helper to draw the bar - does not render subcomponents such as the tooltip or tooltip icon.
	 * @param graphics			Graphics2D to help draw our UI
	 * @param x					The x position to draw the bar
	 * @param y					The y position to draw the bar
	 * @param width				The width of the bar
	 * @param fillLength		The amount to fill the bar
	 * @param barColor			The color of the fill
	 * @param notchColor		The color of the notches, or pips
	 * @param backgroundColor	The background color of the bar, which will show as the border + unfilled area of the bar
	 */
	private void drawBar(Graphics2D graphics, int x, int y, int width, int fillLength, Color barColor, Color notchColor, Color backgroundColor)
	{
		int height = config.thickness();

		graphics.setColor(backgroundColor);
		graphics.drawRect(x, y, width - BORDER_SIZE, height - BORDER_SIZE);
		graphics.fillRect(x, y, width, height);

		graphics.setColor(barColor);
		graphics.fillRect(x + BORDER_SIZE,
				y + BORDER_SIZE,
				fillLength - BORDER_SIZE * 2,
				height - BORDER_SIZE * 2);

		graphics.setColor(notchColor);

		//draw the 9 pip separators
		for (int i = 1; i <= 9; i++)
		{
			graphics.fillRect(x + i * (width/10), y + 1,2, height - BORDER_SIZE*2);
		}

	}

	/**
	 * Helper to find how far the bar fill should be.
	 * @param base			The XP from the previous level to the next level
	 * @param current		The current (not total) XP in the skill level
	 * @param fullWidth		The full width of the bar
	 * @return
	 */
	private static int getBarFillLength(int base, int current, int fullWidth)
	{
		final double ratio = (double) current / base;

		if (ratio >= 1)
		{
			return fullWidth;
		}

		return (int) Math.round(ratio * fullWidth);
	}
}

@Getter
@AllArgsConstructor
enum Viewport
{
	CHATBOX("Chatbox", ComponentID.CHATBOX_FRAME,
			new Point(-4, XPBarOverlay.HEIGHT)),

	TOP_LEFT_FIXED("Top Left", InterfaceID.Toplevel.VIEWPORT,
			new Point(0, XPBarOverlay.HEIGHT - 16)),

	TOP_LEFT_PRE_EOC("Top Left", InterfaceID.ToplevelPreEoc.VIEWPORT,
			new Point(0, XPBarOverlay.HEIGHT - 16)),

	TOP_LEFT_STRETCH("Top Left", InterfaceID.ToplevelOsrsStretch.VIEWPORT,
			new Point(4, XPBarOverlay.HEIGHT - 16)),

	MINIMAP("Minimap", ComponentID.MINIMAP_CONTAINER,
			new Point(-4, -171)),

	INVENTORY_FIXED("Inventory", InterfaceID.Toplevel.SIDE_TOP_CONTAINER,
			new Point(0, XPBarOverlay.HEIGHT)),

	INVENTORY_PRE_EOC("Inventory", InterfaceID.ToplevelPreEoc.SIDE_CONTAINER,
			new Point(-4, XPBarOverlay.HEIGHT)),

	INVENTORY_STRETCH("Inventory", InterfaceID.ToplevelOsrsStretch.SIDE_CONTAINER,
			new Point(21, XPBarOverlay.HEIGHT + 37));

	private String type;
	private int viewport;
	private Point offsetLeft;
}

@Getter
@AllArgsConstructor
enum FallbackViewport
{
	CHATBOX_FALLBACK("Chatbox", InterfaceID.Chatbox.CONTROLS,
			new Point(-4, XPBarOverlay.HEIGHT)),

	INVENTORY_FALLBACK("Inventory", InterfaceID.ToplevelPreEoc.SIDE_MOVABLE_LAYER,
			new Point(-4, XPBarOverlay.HEIGHT));

	private String type;
	private int viewport;
	private Point offsetLeft;
}