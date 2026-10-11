/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.gui.clickgui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
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
 * <p>The layout is responsive: panels are laid out in a grid that always fits
 * inside the screen. When the window is too small to show every category, all
 * panels are force-collapsed so that the category headers stay visible and
 * reachable instead of drifting off-screen.
 */
public final class ClickGuiScreen extends Screen
{
	private static final int HEADER_HEIGHT = 16;
	private static final int ROW_HEIGHT = 14;

	private static final int PREFERRED_WIDTH = 120;
	private static final int MIN_WIDTH = 84;
	private static final int GAP = 6;
	private static final int MARGIN = 8;
	private static final int TOP = 12;

	/** Vertical space reserved for chroma/borders in the scroll maths. */
	private static final int SCROLL_PADDING = 60;

	private final List<CategoryPanel> panels = new ArrayList<>();
	private CategoryPanel dragging;
	private int dragX;
	private int dragY;

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

	private static void setRebinding(boolean value)
	{
		rebinding = value;
	}

	@Override
	protected void init()
	{
		panels.clear();

		ModuleCategory[] categories = ModuleCategory.values();
		List<List<Module>> moduleLists = new ArrayList<>();

		for(ModuleCategory category : categories)
			moduleLists.add(ModuleManager.getByCategory(category));

		// Work out a column count that keeps every panel on-screen. We prefer
		// the fewest rows (widest columns) that fit horizontally; if even a
		// single column of minimum-width panels does not fit we still clamp to
		// one column so nothing is pushed off the right edge.
		int available = Math.max(MIN_WIDTH, width - MARGIN * 2);
		int maxColumns = Math.max(1, (available + GAP) / (MIN_WIDTH + GAP));
		int columns = Math.min(Math.max(1, categories.length), maxColumns);

		int panelWidth = computePanelWidth(columns, available);

		// If we cannot give every panel a readable width, or the resulting
		// grid is taller than the screen even when collapsed, force-collapse
		// everything so the headers remain visible.
		ClickGuiModule config = config();
		boolean collapseAllowed = config == null || config.forceCollapse();
		boolean tooNarrow = columns < categories.length;
		int rowsNeeded = (categories.length + columns - 1) / columns;
		boolean tooShort = TOP + rowsNeeded * (HEADER_HEIGHT + GAP) > height;
		forceCollapsed = collapseAllowed && tooNarrow && tooShort;

		int gridWidth = columns * panelWidth + (columns - 1) * GAP;
		int startX = Math.max(MARGIN, (width - gridWidth) / 2);

		for(int i = 0; i < categories.length; i++)
		{
			int col = i % columns;
			int row = i / columns;

			int px = startX + col * (panelWidth + GAP);
			int py = TOP + row * (HEADER_HEIGHT + GAP);

			CategoryPanel panel = new CategoryPanel(categories[i], px, py,
				panelWidth, moduleLists.get(i));
			panel.expanded = !forceCollapsed && !moduleLists.get(i).isEmpty();
			panels.add(panel);
		}
	}

	private int computePanelWidth(int columns, int available)
	{
		int width = (available - (columns - 1) * GAP) / columns;
		// Never exceed the preferred width, never go under the minimum.
		return Math.max(MIN_WIDTH, Math.min(PREFERRED_WIDTH, width));
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY,
		float delta)
	{
		// Dim the world behind the GUI.
		context.fill(0, 0, width, height, 0xB0101014);

		for(CategoryPanel panel : panels)
			panel.render(context, mouseX, mouseY, delta, font);

		if(forceCollapsed)
		{
			String hint = "Screen too small - all categories collapsed.";
			context.drawString(font, hint, MARGIN,
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
			context.drawString(font, hint, hx, hy, 0xFFFFFFFF);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled)
	{
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();

		for(CategoryPanel panel : panels)
		{
			if(panel.mouseClicked(mouseX, mouseY, button))
			{
				if(button == 0 && panel.isHeaderHovered(mouseX, mouseY))
				{
					dragging = panel;
					dragX = (int)(mouseX - panel.x());
					dragY = (int)(mouseY - panel.y());
					panels.remove(panel);
					panels.add(panel);
				}
				return true;
			}
		}

		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event)
	{
		dragging = null;
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX,
		double dragY)
	{
		if(dragging != null)
		{
			dragging.setPosition((int)(event.x() - this.dragX),
				(int)(event.y() - this.dragY));
			return true;
		}

		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal,
		double vertical)
	{
		for(CategoryPanel panel : panels)
			if(panel.mouseScrolled(mouseX, mouseY, vertical))
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

		if(NovaClient.INSTANCE != null)
			NovaClient.INSTANCE.getConfig().save();

		if(minecraft != null)
			minecraft.setScreen(null);
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

	/** A draggable, collapsible category panel. */
	private final class CategoryPanel
	{
		private final ModuleCategory category;
		private final List<Module> modules;

		private int x;
		private int y;
		private final int width;
		private final int headerHeight = HEADER_HEIGHT;

		private boolean expanded = true;
		private int scroll;
		private Module expandedModule;
		private final List<SettingRow> settingRows = new ArrayList<>();

		/** Module currently waiting for a key press to rebind, or null. */
		private Module rebinding;

		private CategoryPanel(ModuleCategory category, int x, int y, int width,
			List<Module> modules)
		{
			this.category = category;
			this.x = x;
			this.y = y;
			this.width = width;
			this.modules = modules;

			// Start collapsed if there is nothing to show.
			this.expanded = !modules.isEmpty();
		}

		private int x()
		{
			return x;
		}

		private int y()
		{
			return y;
		}

		private void setPosition(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		/** Height of the expanded module body (keybind row + settings). */
		private int expandedBody()
		{
			// One row for the keybind, plus one per setting, plus padding.
			return (settingRows.size() + 1) * ROW_HEIGHT + 4;
		}

		private int bodyHeight()
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

			return h + 4;
		}

		/** Height of the body actually usable without running off-screen. */
		private int maxVisibleBody()
		{
			return Math.max(ROW_HEIGHT, height - y - headerHeight
				- SCROLL_PADDING);
		}

		private boolean isHeaderHovered(double mouseX, double mouseY)
		{
			return mouseX >= x && mouseX <= x + width && mouseY >= y
				&& mouseY <= y + headerHeight;
		}

		private void rebuildSettingRows(Module module)
		{
			settingRows.clear();
			Settings settings = module.settings;
			for(Setting<?> setting : settings.all())
				settingRows.add(new SettingRow(setting));
		}

		private void render(GuiGraphics context, int mouseX, int mouseY,
			float delta, net.minecraft.client.gui.Font font)
		{
			ClickGuiModule config = config();
			int opacity = config == null ? 235 : config.opacity();

			// Header with the Nova gradient.
			context.fillGradient(x, y, x + width, y + headerHeight,
				GuiTheme.PRIMARY_TOP, GuiTheme.PRIMARY_BOTTOM);
			context.drawString(font, category.displayName(), x + 5,
				y + (headerHeight - 8) / 2 + 1, 0xFFFFFFFF);
			context.drawString(font, expanded ? "-" : "+", x + width - 10,
				y + (headerHeight - 8) / 2 + 1, 0xFFFFFFFF);

			if(!expanded)
				return;

			int bodyTop = y + headerHeight;
			int bodyBottom = bodyTop + bodyHeight();

			// Panel background.
			context.fill(x, bodyTop, x + width, bodyBottom,
				(GuiTheme.PANEL & 0x00FFFFFF) | (Math.min(opacity, 235) << 24));

			int rowY = bodyTop + 2 - scroll;

			for(Module module : modules)
			{
				boolean hovered = mouseX >= x && mouseX <= x + width
					&& mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT;

				int rowColor = module.isEnabled() ? GuiTheme.PRIMARY_TOP
					: GuiTheme.PANEL_HEADER;
				if(hovered && !module.isEnabled())
					rowColor = GuiTheme.PANEL_HEADER + 0x060606;

				context.fill(x + 2, rowY, x + width - 2, rowY + ROW_HEIGHT,
					rowColor);

				int textColor = module.isEnabled() ? 0xFFFFFFFF
					: GuiTheme.TEXT;

				String label = module.displayName();
				String info = module.getInfoString();
				if(info != null && !info.isEmpty())
					label = label + " " + info;

				context.drawString(font, label, x + 6, rowY + 3, textColor);

				if(module == expandedModule)
				{
					int sy = rowY + ROW_HEIGHT;
					renderKeybindRow(context, font, module, x + 2,
						sy, width - 4, mouseX, mouseY);
					sy += ROW_HEIGHT;

					for(SettingRow row : settingRows)
					{
						row.render(context, font, x + 6, sy, width - 12,
							mouseX, mouseY);
						sy += ROW_HEIGHT;
					}
				}

				rowY += ROW_HEIGHT;
				if(module == expandedModule)
					rowY += expandedBody();
			}

			// Border.
			context.renderOutline(x, y, x + width, bodyBottom,
				GuiTheme.PANEL_BORDER);
		}

		/** Draws the "Keybind" row for an expanded module. */
		private void renderKeybindRow(GuiGraphics context,
			net.minecraft.client.gui.Font font, Module module, int x, int y,
			int width, int mouseX, int mouseY)
		{
			boolean waiting = rebinding == module;
			boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y
				&& mouseY <= y + ROW_HEIGHT;

			if(waiting)
				context.fill(x, y, x + width, y + ROW_HEIGHT,
					GuiTheme.PRIMARY_TOP);
			else if(hovered)
				context.fill(x, y, x + width, y + ROW_HEIGHT, GuiTheme.HOVER);

			String value = waiting ? "<press key>"
				: KeyNames.of(module.keybind());

			context.drawString(font, "Keybind", x + 4, y + 3,
				waiting ? 0xFFFFFFFF : GuiTheme.TEXT_DIM);
			context.drawString(font, value, x + width - font.width(value) - 6,
				y + 3, waiting ? 0xFFFFFFFF : GuiTheme.TEXT);
		}

		private boolean mouseClicked(double mouseX, double mouseY, int button)
		{
			if(mouseX < x || mouseX > x + width || mouseY < y)
				return false;

			if(isHeaderHovered(mouseX, mouseY))
			{
				if(button == 1)
					expanded = !expanded;
				return true;
			}

			if(!expanded)
				return false;

			int bodyTop = y + headerHeight;
			int rowY = bodyTop + 2 - scroll;

			for(Module module : modules)
			{
				if(mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT)
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

						if(expandedModule != null)
							rebuildSettingRows(module);
					}
					return true;
				}

				if(module == expandedModule)
				{
					int sy = rowY + ROW_HEIGHT;

					// Keybind row (first row of the expanded body).
					if(mouseY >= sy && mouseY <= sy + ROW_HEIGHT)
					{
						if(button == 0)
						{
							rebinding = rebinding == module ? null : module;
							// Only one module can listen at a time.
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
						if(row.mouseClicked(mouseX, mouseY, x + 6, sy,
							width - 12, button))
							return true;
						sy += ROW_HEIGHT;
					}
				}

				rowY += ROW_HEIGHT;
				if(module == expandedModule)
					rowY += expandedBody();
			}

			return true;
		}

		private boolean mouseScrolled(double mouseX, double mouseY,
			double vertical)
		{
			if(mouseX < x || mouseX > x + width || mouseY < y
				|| mouseY > y + headerHeight + bodyHeight())
				return false;

			int total = bodyHeight();
			int visible = maxVisibleBody();
			if(total <= visible)
				return false;

			scroll -= (int)(vertical * 12);
			scroll = Math.max(0, Math.min(scroll, total - visible));
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

		private void render(GuiGraphics context,
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

			context.drawString(font, setting.name(), x, y + 3,
				GuiTheme.TEXT_DIM);
			context.drawString(font, value, x + width - font.width(value) - 2,
				y + 3, GuiTheme.TEXT);
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
