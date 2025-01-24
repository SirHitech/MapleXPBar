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
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.SkillColor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;


import net.runelite.client.ui.overlay.OverlayPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

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
	public Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MapleXPBarConfig config;

	@Getter(AccessLevel.PACKAGE)
	private boolean barsDisplayed;

	@Getter(AccessLevel.PACKAGE)
	private Skill currentSkill;

	private final Map<Skill, Integer> skillList = new EnumMap<>(Skill.class);

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
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

		log.info("State CHANGED: " + statChanged.getSkill());
	}
}

@Slf4j
class XPBarOverlay extends Overlay
{
	private MapleXPBarConfig config;
	private Client client;
	private static final Logger logger = LoggerFactory.getLogger(XPBarOverlay.class);
	private static final Color BACKGROUND = new Color(0, 0, 0, 255);
	private static final int WIDTH = 512;
	static final int HEIGHT = 4;
	private static final int BORDER_SIZE = 1;
	private int currentXP;
	private int currentLevel;
	private int nextLevelXP;

	private final MapleXPBarPlugin plugin;
	private final SpriteManager spriteManager;

	@Inject
	private XPBarOverlay(Client client, MapleXPBarPlugin plugin, MapleXPBarConfig config, SkillIconManager skillIconManager, SpriteManager spriteManager)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.spriteManager = spriteManager;
	}

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

		Viewport curViewport = null;
		Widget curWidget = null;

		for (Viewport viewport : Viewport.values())
		{
			final Widget viewportWidget = client.getWidget(viewport.getViewport());
			if (viewportWidget != null)
			{
				curViewport = viewport;
				curWidget = viewportWidget;
				break;
			}
		}

		if (curViewport == null)
		{
			return null;
		}

		boolean isChatboxUnloaded = curWidget.getCanvasLocation().equals(new Point(-1, -1));

		final Point offset = curViewport.getOffsetLeft();
		final Point location = isChatboxUnloaded ? new Point(0, client.getCanvasHeight() - 165) : curWidget.getCanvasLocation();
		final int height, offsetBarX, offsetBarY;

		boolean automaticallyOffsetBar = (
				config.scalingModifier() == 100
				&& config.manualOffsetX() == 0
				&& config.manualOffsetY() == 0
		);

		int chatboxHiddenOffset = curWidget.isHidden() && automaticallyOffsetBar ? 142 : 0;
		height = HEIGHT;
		offsetBarX = (location.getX() - offset.getX());
		offsetBarY = (location.getY() - offset.getY() + chatboxHiddenOffset);

		renderBar(g, config.displayHealthAndPrayer(), offsetBarX, offsetBarY, height);

		return null;
	}

	private String getTootltipText(int currentLevelXP, int nextLevelXP)
	{
		//Format tooltip display
		NumberFormat f = NumberFormat.getNumberInstance(Locale.US);
		String xpText = f.format(currentXP) + "/" + f.format(nextLevelXP);
		Double percentage = 100.0 * (currentXP - currentLevelXP) / (nextLevelXP - currentLevelXP);

		if (config.showPercentage())
		{
			xpText = config.showOnlyPercentage() ? f.format(percentage) + "%" : xpText + " (" + f.format(percentage) + "%)";
		}

		return xpText;
	}

	public void renderBar(Graphics2D graphics, boolean render3bars, int x, int y, int height)
	{
		//Get info for experience
		Skill skill = config.mostRecentSkill() ? plugin.getCurrentSkill() : config.skill();
		currentXP = client.getSkillExperience(skill);
		currentLevel = Experience.getLevelForXp(currentXP);
		nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
		int currentLevelXP = Experience.getXpForLevel(currentLevel);
		boolean isTransparentChatbox = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;

		//Get info for hp and pray
		int currentHP = client.getBoostedSkillLevel(Skill.HITPOINTS);
		int maxHP = client.getRealSkillLevel(Skill.HITPOINTS);
		int currentPray = client.getBoostedSkillLevel(Skill.PRAYER);
		int maxPray = client.getRealSkillLevel(Skill.PRAYER);

		//Calc starting position for bar
		int adjustedX = x;
		int adjustedY;
		int adjustedWidth = WIDTH;

		int manualOffsetX = config.manualOffsetX();
		int manualOffsetY = config.manualOffsetY();

		if (client.isResized()){
			adjustedX = x - 4;
			adjustedWidth = WIDTH + 7;
		}

		//Transparent chatbox looks smaller - adjust if shown
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

		boolean automaticallyOffsetBar = (
				config.scalingModifier() == 100
				&& config.manualOffsetX() == 0
				&& config.manualOffsetY() == 0
		);

		adjustedY = client.isResized() && isTransparentChatbox && isChatShown && automaticallyOffsetBar? y + 7: y;

		adjustedX += manualOffsetX;
		adjustedY += manualOffsetY;

		double scale = config.scalingModifier() / 100f;
		AffineTransform scalingTransform = AffineTransform.getScaleInstance(scale,scale);
		graphics.transform(scalingTransform);

		final int filledWidthXP = getBarWidth(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedWidth);
		final int filledWidthHP = getBarWidth(maxHP, currentHP, adjustedWidth);
		final int filledWidthPray = getBarWidth(maxPray, currentPray, adjustedWidth);

		String xpText = getTootltipText(currentLevelXP, nextLevelXP);

		boolean	hoveringBar = client.getMouseCanvasPosition().getX() >= (int)(adjustedX*scale) && client.getMouseCanvasPosition().getY() >= (int)(adjustedY*scale)
				&& client.getMouseCanvasPosition().getX() <= (int)((adjustedX + adjustedWidth)*scale) && client.getMouseCanvasPosition().getY() <= (int)((adjustedY + HEIGHT)*scale);

		if (hoveringBar || config.alwaysShowTooltip())
		{
			int THREE_BAR_OFFSET = render3bars ? HEIGHT*2 : 0;
			graphics.setColor(config.colorXPText());
			graphics.drawString(xpText, manualOffsetX + (adjustedWidth/2 + 8) - (xpText.length()*3), adjustedY-THREE_BAR_OFFSET);
		}

		Color barColor;

		//Render the overlay
		if (config.mostRecentSkillColor())
		{
			if (config.mostRecentSkill())
			{
				//As long as there is a recent skill, find it. Otherwise, stop rendering the bar
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

		drawBar(graphics, adjustedX, adjustedY, adjustedWidth, filledWidthXP, barColor, config.colorXPNotches());

		if (render3bars){
			drawBar(graphics, adjustedX, adjustedY-HEIGHT, adjustedWidth, filledWidthPray, config.colorPray(), config.colorPrayNotches());
			drawBar(graphics, adjustedX, adjustedY-(HEIGHT*2), adjustedWidth, filledWidthHP, config.colorHP(), config.colorHPNotches());
		}
	}

	private void drawBar(Graphics2D graphics, int adjustedX, int adjustedY, int adjustedWidth, int fill, Color barColor, Color notchColor)
	{

		graphics.setColor(BACKGROUND);
		graphics.drawRect(adjustedX, adjustedY, adjustedWidth - BORDER_SIZE, HEIGHT - BORDER_SIZE);
		graphics.fillRect(adjustedX, adjustedY, adjustedWidth, HEIGHT);

		graphics.setColor(barColor);
		graphics.fillRect(adjustedX + BORDER_SIZE,
				adjustedY + BORDER_SIZE,
				fill - BORDER_SIZE * 2,
				HEIGHT - BORDER_SIZE * 2);

		graphics.setColor(notchColor);

		//draw the 9 pip separators
		for (int i = 1; i <= 9; i++)
		{
			graphics.fillRect(adjustedX + i * (adjustedWidth/10), adjustedY + 1,2, HEIGHT - BORDER_SIZE*2);
		}

	}

	private static int getBarWidth(int base, int current, int size)
	{
		final double ratio = (double) current / base;

		if (ratio >= 1)
		{
			return size;
		}

		return (int) Math.round(ratio * size);
	}
}

@Getter
@AllArgsConstructor
enum Viewport
{
	FIXED(ComponentID.FIXED_VIEWPORT_FIXED_VIEWPORT, ComponentID.CHATBOX_FRAME,
			new Point(-4, XPBarOverlay.HEIGHT));

	private int container;
	private int viewport;
	private Point offsetLeft;
}