/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.gui.clickgui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.novaclient.NovaClient;
import net.novaclient.gui.GuiTheme;
import net.novaclient.gui.KeyNames;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.client.ClickGuiModule;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.Setting;
import net.novaclient.setting.Settings;

/**
 * Meteor-style ClickGUI with Nova theming (gradient rgb(37,99,235) ->
 * rgb(78,72,230)).
 *
 * <p>Like Meteor's window system, each category is an <b>independent,
 * draggable window</b> rather than a cell in a fixed grid. Window positions
 * are remembered in {@code config/clickgui.json}, so panels never pile up on
 * top of each other and the user can arrange them however they like. The
 * default layout lays the windows out in rows, offsetting each row by the
 * <i>actual</i> height of the previous one.
 */
public final class ClickGuiScreen extends Screen
{
	private static final int HEADER_HEIGHT = 13;
	private static final int ROW_HEIGHT = 11;
	private static final int FRAME_PAD = 3;

	private static final int WINDOW_WIDTH = 104;
	private static final int GAP = 4;
	private static final int MARGIN = 6;

	/** Fraction of the screen height a window body may occupy. */
	private static final double DEFAULT_BODY_RATIO = 0.72;

	private final List<CategoryPanel> panels = new ArrayList<>();
	private CategoryPanel dragging;
	private int dragOffX;
	private int dragOffY;

	/** True when the screen is too small to fit everything and we collapsed. */
	private boolean forceCollapsed;

	/**
	 * True while an open ClickGUI is waiting for a key press to rebind a
	 * module. Used by {@link ModuleManager} to avoid toggling modules with
	 * the key that is being bound.
	 */
	private static boolean rebinding;

	public ClickGuiScreen()
	{
		super(Component.literal("Nova ClickGUI"));
	}

	/** Whether a ClickGUI is currently capturing a key for rebinding. */
	public static boolean isRebinding()
	{
		return rebinding;
	}

	/** Persists module settings and keybinds to disk immediately. */
	public static void saveConfig()
	{
		if(NovaClient.INSTANCE != null)
			NovaClient.INSTANCE.getConfig().save();
	}

	/** Persists the ClickGUI window layout (positions + expanded state). */
	private void saveLayout()
	{
		if(NovaClient.INSTANCE == null)
			return;

		Map<String, double[]> windows = new HashMap<>();
		for(CategoryPanel panel : panels)
			windows.put(panel.category.name(),
				new double[] {panel.x, panel.y, panel.expanded ? 1 : 0});

		NovaClient.INSTANCE.getConfig().saveClickGuiWindows(windows);
	}

	private static void setRebinding(boolean value)
	{
		rebinding = value;
	}

	@Override
	protected void init()
	{
		panels.clear();

		ModuleCategory[] categories = ModuleCategory.values();
		Map<String, double[]> saved =
			NovaClient.INSTANCE == null ? new HashMap<>()
				: NovaClient.INSTANCE.getConfig().loadClickGuiWindows();

		List<List<Module>> moduleLists = new ArrayList<>();
		for(ModuleCategory category : categories)
			moduleLists.add(ModuleManager.getByCategory(category));

		// Body height available to any single window.
		int bodyLimit = (int)(height * DEFAULT_BODY_RATIO);
		int rowStep = HEADER_HEIGHT + bodyLimit + FRAME_PAD + GAP;

		// Default layout: lay windows out in rows, wrapping when the next
		// one would run off the right edge. Every window in a row shares the
		// same *default* height so the rows never overlap, no matter how
		// much content each category has (overflow scrolls inside).
		int curX = MARGIN;
		int curY = MARGIN;

		for(int i = 0; i < categories.length; i++)
		{
			ModuleCategory category = categories[i];

			// Wrap to a new row if this window would not fit.
			if(curX + WINDOW_WIDTH > width - MARGIN && curX > MARGIN)
			{
				curX = MARGIN;
				curY += rowStep;
			}

			double[] pos = saved.get(category.name());
			int px;
			int py;
			boolean expanded;

			if(pos != null && pos[0] >= 0 && pos[1] >= 0)
			{
				// Use the saved position (clamped on-screen below).
				px = (int)pos[0];
				py = (int)pos[1];
				expanded = pos[2] != 0;
			}
			else
			{
				px = curX;
				py = curY;
				expanded = !moduleLists.get(i).isEmpty();

				curX += WINDOW_WIDTH + GAP;
			}

			CategoryPanel panel = new CategoryPanel(category, px, py,
				WINDOW_WIDTH, moduleLists.get(i), bodyLimit);
			panel.expanded = expanded && !moduleLists.get(i).isEmpty();
			panels.add(panel);
		}

		// Clamp every window fully on-screen (so nothing is unreachable
		// after a resolution change).
		for(CategoryPanel panel : panels)
			clampOnScreen(panel);
	}

	/** Keeps a panel's header reachable and its body inside the screen. */
	private void clampOnScreen(CategoryPanel panel)
	{
		int h = panel.totalHeight();

		if(panel.x + panel.width > width - MARGIN)
			panel.x = Math.max(MARGIN, width - MARGIN - panel.width);
		if(panel.x < MARGIN)
			panel.x = MARGIN;

		if(panel.y + h > height - MARGIN)
			panel.y = Math.max(MARGIN, height - MARGIN - h);
		if(panel.y < MARGIN)
			panel.y = MARGIN;
	}

	/** Height of the longest category (used only for the layout hint). */
	private boolean tooSmall()
	{
		return width < WINDOW_WIDTH + MARGIN * 2;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY,
		float delta)
	{
		// Dim the world behind the GUI.
		context.fill(0, 0, width, height, 0xB0101014);

		for(CategoryPanel panel : panels)
			panel.render(context, mouseX, mouseY, delta, font);

		if(tooSmall() || forceCollapsed)
		{
			String hint = "Screen too small for the ClickGUI.";
			context.text(font, hint, MARGIN,
				height - font.lineHeight - 4, GuiTheme.TEXT_DIM);
		}

		// Rebinding hint, centred at the bottom.
		Module pending = rebindingModule();
		if(pending != null)
		{
			String hint = "Press a key to bind " + pending.displayName()
				+ " - Esc/Delete to clear.";
			int w = font.width(hint);
			int hx = (width - w) / 2;
			int hy = height - font.lineHeight - 4;
			context.fill(hx - 4, hy - 3, hx + w + 4,
				hy + font.lineHeight + 1, 0xCC16161C);
			context.text(font, hint, hx, hy, 0xFFFFFFFF);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled)
	{
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();

		// Iterate back-to-front: the panel drawn last sits on top, so it
		// must receive the click first. This is what fixes clicks landing
		// on the window *behind* the one under the cursor.
		for(int i = panels.size() - 1; i >= 0; i--)
		{
			CategoryPanel panel = panels.get(i);
			if(panel.mouseClicked(mouseX, mouseY, button))
			{
				// Bring the interacted panel to the front.
				if(button == 0 && panel.isHeaderHovered(mouseX, mouseY))
				{
					dragging = panel;
					dragOffX = (int)(mouseX - panel.x);
					dragOffY = (int)(mouseY - panel.y);
				}

				panels.remove(panel);
				panels.add(panel);
				return true;
			}
		}

		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event)
	{
		if(dragging != null)
		{
			dragging = null;
			saveLayout();
		}

		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX,
		double dragY)
	{
		if(dragging != null)
		{
			dragging.x = (int)(event.x() - dragOffX);
			dragging.y = (int)(event.y() - dragOffY);
			clampOnScreen(dragging);
			return true;
		}

		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal,
		double vertical)
	{
		// Only the topmost panel under the cursor scrolls.
		for(int i = panels.size() - 1; i >= 0; i--)
			if(panels.get(i).mouseScrolled(mouseX, mouseY, vertical))
				return true;

		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}

	@Override
	public boolean keyPressed(KeyEvent event)
	{
		// If a module is waiting for a key, capture it here.
		Module target = rebindingModule();
		if(target != null)
		{
			// Esc or Delete unbinds; any other key binds.
			if(event.key() == 256 || event.key() == 261)
				target.setKeybind(-1);
			else
				target.setKeybind(event.key());

			clearRebinding();
			saveConfig();
			return true;
		}

		// Esc closes the GUI.
		if(event.key() == 256)
		{
			onClose();
			return true;
		}

		return super.keyPressed(event);
	}

	/** Returns the module currently awaiting a key, or null. */
	private Module rebindingModule()
	{
		for(CategoryPanel panel : panels)
			if(panel.rebinding != null)
				return panel.rebinding;

		return null;
	}

	private void clearRebinding()
	{
		for(CategoryPanel panel : panels)
			panel.rebinding = null;

		setRebinding(false);
	}

	/** Recomputes the static "capturing a key" flag from panel state. */
	private void syncRebindingFlag()
	{
		setRebinding(rebindingModule() != null);
	}

	@Override
	public void onClose()
	{
		// Stop listening for keys and persist any GUI changes.
		clearRebinding();
		saveLayout();

		if(NovaClient.INSTANCE != null)
			NovaClient.INSTANCE.getConfig().save();

		if(minecraft != null)
			minecraft.gui.setScreen(null);
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	private ClickGuiModule config()
	{
		return ModuleManager.get(ClickGuiModule.class);
	}

	/** A draggable, collapsible category window. */
	private final class CategoryPanel
	{
		private final ModuleCategory category;
		private final List<Module> modules;

		private int x;
		private int y;
		private final int width;
		private final int bodyLimit;

		private boolean expanded = true;
		private int scroll;
		private Module expandedModule;
		private final List<SettingRow> settingRows = new ArrayList<>();

		/** Module currently waiting for a key press to rebind, or null. */
		private Module rebinding;

		private CategoryPanel(ModuleCategory category, int x, int y, int width,
			List<Module> modules, int bodyLimit)
		{
			this.category = category;
			this.x = x;
			this.y = y;
			this.width = width;
			this.modules = modules;
			this.bodyLimit = bodyLimit;

			this.expanded = !modules.isEmpty();
		}

		private boolean isHeaderHovered(double mouseX, double mouseY)
		{
			return mouseX >= x && mouseX <= x + width && mouseY >= y
				&& mouseY <= y + HEADER_HEIGHT;
		}

		/** Height of the expanded module body (keybind row + settings). */
		private int expandedBody()
		{
			return (settingRows.size() + 1) * ROW_HEIGHT + 4;
		}

		/** Full content height of the body, before clamping to the screen. */
		private int fullBodyHeight()
		{
			if(!expanded)
				return 0;

			int h = 0;
			for(Module module : modules)
			{
				h += ROW_HEIGHT;
				if(module == expandedModule)
					h += expandedBody();
			}

			return h + 2;
		}

		/**
		 * Visible body height. Depends only on the content and the screen,
		 * never on the window's own y, so clamping the window position stays
		 * stable (no feedback loop between y and height).
		 */
		private int visibleBodyHeight()
		{
			int full = fullBodyHeight();
			return Math.max(0, Math.min(full, bodyLimit));
		}

		/** Total on-screen height of the whole window. */
		private int totalHeight()
		{
			int body = visibleBodyHeight();
			return HEADER_HEIGHT + (body > 0 ? body + FRAME_PAD : 0);
		}

		private void rebuildSettingRows(Module module)
		{
			settingRows.clear();
			Settings settings = module.settings;
			for(Setting<?> setting : settings.all())
				settingRows.add(new SettingRow(setting));
		}

		private void render(GuiGraphicsExtractor context, int mouseX, int mouseY,
			float delta, net.minecraft.client.gui.Font font)
		{
			ClickGuiModule config = config();
			int opacity = config == null ? 235 : config.opacity();

			int bodyVisible = visibleBodyHeight();
			int bodyTop = y + HEADER_HEIGHT;
			int frameBottom = y + totalHeight();

			// Window frame (behind header + body).
			context.fill(x, y, x + width, frameBottom,
				(GuiTheme.PANEL & 0x00FFFFFF) | (Math.min(opacity, 235) << 24));

			// Header with the Nova gradient.
			context.fillGradient(x, y, x + width, y + HEADER_HEIGHT,
				GuiTheme.PRIMARY_TOP, GuiTheme.PRIMARY_BOTTOM);
			context.text(font, category.displayName(), x + 4,
				y + (HEADER_HEIGHT - 8) / 2 + 1, 0xFFFFFFFF);
			context.text(font, expanded ? "-" : "+", x + width - 8,
				y + (HEADER_HEIGHT - 8) / 2 + 1, 0xFFFFFFFF);

			// Border.
			context.outline(x, y, x + width, frameBottom,
				GuiTheme.PANEL_BORDER);

			if(!expanded)
				return;

			int contentTop = bodyTop + 1;
			int contentBottom = contentTop + bodyVisible;

			// Clip by simply not drawing rows outside the visible body.
			int full = fullBodyHeight();
			int maxScroll = Math.max(0, full - bodyVisible);

			// Scrollbar (thin) when the content overflows.
			if(maxScroll > 0)
			{
				int trackX = x + width - 3;
				int trackTop = contentTop;
				int trackH = bodyVisible;
				int thumbH = Math.max(8,
					(int)((double)trackH * bodyVisible / full));
				int thumbY = trackTop
					+ (int)((double)(trackH - thumbH) * scroll / maxScroll);
				context.fill(trackX, trackTop, trackX + 2, trackTop + trackH,
					0x40000000);
				context.fill(trackX, thumbY, trackX + 2, thumbY + thumbH,
					GuiTheme.PRIMARY_TOP);
			}

			// Rows.
			int rowY = contentTop - scroll;

			for(Module module : modules)
			{
				boolean visible = rowY + ROW_HEIGHT > contentTop
					&& rowY < contentBottom;

				boolean hovered = mouseX >= x && mouseX <= x + width
					&& mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT;

				if(visible)
				{
					int rowColor = module.isEnabled() ? GuiTheme.PRIMARY_TOP
						: GuiTheme.PANEL_HEADER;
					if(hovered && !module.isEnabled())
						rowColor = GuiTheme.PANEL_HEADER + 0x060606;

					context.fill(x + 1, rowY, x + width - 1,
						rowY + ROW_HEIGHT, rowColor);

					int textColor = module.isEnabled() ? 0xFFFFFFFF
						: GuiTheme.TEXT;

					String label = module.displayName();
					String info = module.getInfoString();
					if(info != null && !info.isEmpty())
						label = label + " " + info;

					context.text(font, label, x + 4, rowY + 2, textColor);
				}

				if(module == expandedModule)
				{
					int sy = rowY + ROW_HEIGHT;

					if(visible)
						renderKeybindRow(context, font, module, x + 1, sy,
							width - 2, mouseX, mouseY, contentTop,
							contentBottom);

					sy += ROW_HEIGHT;

					for(SettingRow row : settingRows)
					{
						if(sy + ROW_HEIGHT > contentTop && sy < contentBottom)
							row.render(context, font, x + 4, sy, width - 8,
								mouseX, mouseY);

						sy += ROW_HEIGHT;
					}
				}

				rowY += ROW_HEIGHT;
				if(module == expandedModule)
					rowY += expandedBody();
			}
		}

		/** Draws the "Keybind" row for an expanded module. */
		private void renderKeybindRow(GuiGraphicsExtractor context,
			net.minecraft.client.gui.Font font, Module module, int x, int y,
			int width, int mouseX, int mouseY, int clipTop, int clipBottom)
		{
			if(y + ROW_HEIGHT <= clipTop || y >= clipBottom)
				return;

			boolean waiting = rebinding == module;
			boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y
				&& mouseY <= y + ROW_HEIGHT;

			if(waiting)
				context.fill(x, y, x + width, y + ROW_HEIGHT,
					GuiTheme.PRIMARY_TOP);
			else if(hovered)
				context.fill(x, y, x + width, y + ROW_HEIGHT, GuiTheme.HOVER);

			String value = waiting ? "<key>" : KeyNames.of(module.keybind());

			context.text(font, "Key", x + 3, y + 2,
				waiting ? 0xFFFFFFFF : GuiTheme.TEXT_DIM);
			context.text(font, value, x + width - font.width(value) - 4,
				y + 2, waiting ? 0xFFFFFFFF : GuiTheme.TEXT);
		}

		private boolean mouseClicked(double mouseX, double mouseY, int button)
		{
			if(mouseX < x || mouseX > x + width || mouseY < y
				|| mouseY > y + totalHeight())
				return false;

			if(isHeaderHovered(mouseX, mouseY))
			{
				// Right-click toggles collapse; left-click starts a drag
				// (handled by the screen so it can bring us to the front).
				if(button == 1)
				{
					expanded = !expanded;
					scroll = 0;
					saveLayout();
				}

				return true;
			}

			if(!expanded)
				return false;

			int contentTop = y + HEADER_HEIGHT + 1;
			int contentBottom = contentTop + visibleBodyHeight();
			int rowY = contentTop - scroll;

			for(Module module : modules)
			{
				boolean rowVisible = rowY + ROW_HEIGHT > contentTop
					&& rowY < contentBottom;
				boolean inRow = mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT;

				if(rowVisible && inRow && mouseY >= contentTop
					&& mouseY <= contentBottom)
				{
					if(button == 0)
					{
						module.toggle();
						// Meteor-style chat feedback on toggle.
						module.sendToggledMsg();
					}
					else if(button == 1 || button == 2)
					{
						expandedModule =
							expandedModule == module ? null : module;
						rebinding = null;
						scroll = 0;

						if(expandedModule != null)
							rebuildSettingRows(module);
					}
					return true;
				}

				if(module == expandedModule)
				{
					int sy = rowY + ROW_HEIGHT;

					// Keybind row (first row of the expanded body).
					if(sy + ROW_HEIGHT > contentTop && sy < contentBottom
						&& mouseY >= sy && mouseY <= sy + ROW_HEIGHT)
					{
						if(button == 0)
						{
							rebinding = rebinding == module ? null : module;
							if(rebinding != null)
								for(CategoryPanel p : panels)
									if(p != this)
										p.rebinding = null;

							syncRebindingFlag();
						}
						else if(button == 2)
						{
							rebinding = null;
							syncRebindingFlag();
						}

						return true;
					}

					sy += ROW_HEIGHT;

					for(SettingRow row : settingRows)
					{
						boolean visible = sy + ROW_HEIGHT > contentTop
							&& sy < contentBottom;
						if(visible && row.mouseClicked(mouseX, mouseY, x + 4,
							sy, width - 8, button))
							return true;
						sy += ROW_HEIGHT;
					}
				}

				rowY += ROW_HEIGHT;
				if(module == expandedModule)
					rowY += expandedBody();
			}

			// Click inside the body but not on a row: consume it so it does
			// not fall through to panels behind this one.
			return mouseY >= contentTop && mouseY <= contentBottom;
		}

		private boolean mouseScrolled(double mouseX, double mouseY,
			double vertical)
		{
			if(mouseX < x || mouseX > x + width || mouseY < y
				|| mouseY > y + totalHeight())
				return false;

			int full = fullBodyHeight();
			int visible = visibleBodyHeight();
			int maxScroll = Math.max(0, full - visible);
			if(maxScroll <= 0)
				return false;

			scroll -= (int)(vertical * 12);
			scroll = Math.max(0, Math.min(scroll, maxScroll));
			return true;
		}
	}

	/** A single setting row inside an expanded module. */
	private static final class SettingRow
	{
		private final Setting<?> setting;

		private SettingRow(Setting<?> setting)
		{
			this.setting = setting;
		}

		private void render(GuiGraphicsExtractor context,
			net.minecraft.client.gui.Font font, int x, int y, int width,
			int mouseX, int mouseY)
		{
			boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y
				&& mouseY <= y + ROW_HEIGHT;

			if(hovered)
				context.fill(x, y, x + width, y + ROW_HEIGHT, GuiTheme.HOVER);

			String value = String.valueOf(setting.get());
			if(setting instanceof BoolSetting bool)
				value = bool.isChecked() ? "on" : "off";

			context.text(font, setting.name(), x, y + 2,
				GuiTheme.TEXT_DIM);
			context.text(font, value, x + width - font.width(value) - 2,
				y + 2, GuiTheme.TEXT);
		}

		private boolean mouseClicked(double mouseX, double mouseY, int x, int y,
			int width, int button)
		{
			if(mouseX < x || mouseX > x + width || mouseY < y
				|| mouseY > y + ROW_HEIGHT)
				return false;

			if(setting instanceof BoolSetting bool)
			{
				bool.setChecked(!bool.isChecked());
				saveConfig();
				return true;
			}

			if(setting instanceof net.novaclient.setting.EnumSetting<?> e)
			{
				nova$cycleEnum(e);
				saveConfig();
			}

			return true;
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		private static void nova$cycleEnum(
			net.novaclient.setting.EnumSetting setting)
		{
			setting.selectNext();
		}
	}
}
