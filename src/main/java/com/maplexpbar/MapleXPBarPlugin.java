package com.maplexpbar;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;


import net.runelite.client.ui.overlay.OverlayPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

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

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		barsDisplayed = true;
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		barsDisplayed = false;
	}

	@Provides
	MapleXPBarConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MapleXPBarConfig.class);
	}
}

@Slf4j
class XPBarOverlay extends Overlay
{
	private MapleXPBarConfig config;
	private Client client;
	private static final Logger logger = LoggerFactory.getLogger(XPBarOverlay.class);
	private static final Color BACKGROUND = new Color(0, 0, 0, 255);
	private static final Color NOTCH_COLOR = new Color(255, 255, 255, 100);
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

		Viewport curViewport = null;
		Widget curWidget = null;

		for (Viewport viewport : Viewport.values())
		{
			final Widget viewportWidget = client.getWidget(viewport.getViewport());
			if (viewportWidget != null && !viewportWidget.isHidden())
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

		final Point offsetLeft = curViewport.getOffsetLeft();
		final Point location = curWidget.getCanvasLocation();
		final int height, offsetLeftBarX, offsetLeftBarY;

		height = HEIGHT;
		offsetLeftBarX = (location.getX() - offsetLeft.getX());
		offsetLeftBarY = (location.getY() - offsetLeft.getY());

		renderBar(g, offsetLeftBarX, offsetLeftBarY, height);
		return null;
	}

	public void renderBar(Graphics2D graphics, int x, int y, int height)
	{
		currentXP = client.getSkillExperience(config.skill());
		currentLevel = Experience.getLevelForXp(currentXP);
		nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
		int currentLevelXP = Experience.getXpForLevel(currentLevel);

		int adjustedX = client.isResized() ? x - 4 : x;
		int adjustedY = client.isResized() ? y + 7: y;
		int adjustedWidth = client.isResized() ? WIDTH + 7 : WIDTH;

		final int filledWidth = getBarWidth(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedWidth);

		NumberFormat f = NumberFormat.getNumberInstance(Locale.US);
		String xpText = f.format(currentXP) + "/" + f.format(nextLevelXP);

		boolean	hoveringBar = client.getMouseCanvasPosition().getX() >= adjustedX && client.getMouseCanvasPosition().getY() >= adjustedY
				&& client.getMouseCanvasPosition().getX() <= adjustedX + adjustedWidth && client.getMouseCanvasPosition().getY() <= adjustedY + HEIGHT;

		if (hoveringBar) graphics.drawString(xpText, (adjustedWidth/2) - (xpText.length()*3), adjustedY);

		graphics.setColor(BACKGROUND);
		graphics.drawRect(adjustedX, adjustedY, adjustedWidth - BORDER_SIZE, HEIGHT - BORDER_SIZE);
		graphics.fillRect(adjustedX, adjustedY, adjustedWidth, HEIGHT);

		graphics.setColor(config.color());
		graphics.fillRect(adjustedX + BORDER_SIZE,
				adjustedY + BORDER_SIZE,
				filledWidth - BORDER_SIZE * 2,
				HEIGHT - BORDER_SIZE * 2);

		graphics.setColor(NOTCH_COLOR);
		graphics.fillRect(adjustedX + 1 * (adjustedWidth/10), adjustedY,2, HEIGHT);
		graphics.fillRect(adjustedX + 2 * (adjustedWidth/10), adjustedY,2, HEIGHT);
		graphics.fillRect(adjustedX + 3 * (adjustedWidth/10), adjustedY,2, HEIGHT);
		graphics.fillRect(adjustedX + 4 * (adjustedWidth/10), adjustedY,2, HEIGHT);
		graphics.fillRect(adjustedX + 5 * (adjustedWidth/10), adjustedY,2, HEIGHT);
		graphics.fillRect(adjustedX + 6 * (adjustedWidth/10), adjustedY,2, HEIGHT);
		graphics.fillRect(adjustedX + 7 * (adjustedWidth/10), adjustedY,2, HEIGHT);
		graphics.fillRect(adjustedX + 8 * (adjustedWidth/10), adjustedY,2, HEIGHT);
		graphics.fillRect(adjustedX + 9 * (adjustedWidth/10), adjustedY,2, HEIGHT);

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
	FIXED(WidgetInfo.FIXED_VIEWPORT, WidgetInfo.CHATBOX,
			new Point(-4, XPBarOverlay.HEIGHT));

	private WidgetInfo container;
	private WidgetInfo viewport;
	private Point offsetLeft;
}