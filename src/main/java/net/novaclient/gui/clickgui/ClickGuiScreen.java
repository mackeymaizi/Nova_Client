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
 */
public final class ClickGuiScreen extends Screen
{
	private static final int PANEL_WIDTH = 120;
	private static final int HEADER_HEIGHT = 16;
	private static final int ROW_HEIGHT = 14;

	private final List<CategoryPanel> panels = new ArrayList<>();
	private CategoryPanel dragging;
	private int dragX;
	private int dragY;

	public ClickGuiScreen()
	{
		super(Component.literal("Nova ClickGUI"));
	}

	@Override
	protected void init()
	{
		panels.clear();

		int x = 10;
		for(ModuleCategory category : ModuleCategory.values())
		{
			List<Module> modules = ModuleManager.getByCategory(category);

			CategoryPanel panel = new CategoryPanel(category, x, 20, PANEL_WIDTH,
				modules);
			panels.add(panel);

			x += PANEL_WIDTH + 6;
		}
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY,
		float delta)
	{
		// Dim the world behind the GUI.
		context.fill(0, 0, width, height, 0xB0101014);

		for(CategoryPanel panel : panels)
			panel.render(context, mouseX, mouseY, delta, font);
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
		// Esc closes the GUI.
		if(event.key() == 256)
		{
			onClose();
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public void onClose()
	{
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

		private int bodyHeight()
		{
			if(!expanded)
				return 0;

			int h = 0;
			for(Module module : modules)
			{
				h += ROW_HEIGHT;
				if(module == expandedModule)
					h += settingRows.size() * ROW_HEIGHT + 4;
			}

			return h + 4;
		}

		private boolean isHeaderHovered(double mouseX, double mouseY)
		{
			return mouseX >= x && mouseX <= x + width && mouseY >= y
				&& mouseY <= y + headerHeight;
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
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
			double scale = config == null ? 1 : config.scale();
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

			int bottom = bodyBottom;
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
					for(SettingRow row : settingRows)
					{
						row.render(context, font, x + 6, sy, width - 12,
							mouseX, mouseY);
						sy += ROW_HEIGHT;
					}
				}

				rowY += ROW_HEIGHT;
				if(module == expandedModule)
					rowY += settingRows.size() * ROW_HEIGHT + 4;
			}

			// Border.
			context.renderOutline(x, y, x + width, bodyBottom,
				GuiTheme.PANEL_BORDER);
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
						module.toggle();
					else if(button == 1)
						expandedModule =
							expandedModule == module ? null : module;
					else if(button == 2)
					{
						expandedModule =
							expandedModule == module ? null : module;
						if(expandedModule != null)
							rebuildSettingRows(module);
					}
					return true;
				}

				if(module == expandedModule)
				{
					int sy = rowY + ROW_HEIGHT;
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
					rowY += settingRows.size() * ROW_HEIGHT + 4;
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
			int visible = height - 60;
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
				return true;
			}

			if(setting instanceof net.novaclient.setting.EnumSetting<?> e)
				nova$cycleEnum(e);

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
