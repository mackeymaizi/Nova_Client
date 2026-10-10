/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.novaclient.NovaClient;
import net.novaclient.event.events.Render2DEvent;
import net.novaclient.gui.GuiTheme;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.module.ModuleManager;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.EnumSetting;

/** Nova's HUD, showing a list of enabled modules in the top-right corner. */
public final class HudModule extends Module
{
	private static final int PADDING = 3;
	private static final int LINE_HEIGHT = 10;

	private final BoolSetting showInfo = new BoolSetting("Info",
		"Shows the module info strings in the HUD.", true);

	private final BoolSetting showKeybinds = new BoolSetting("Keybinds",
		"Shows the keybind next to each module in the HUD.", false);

	private final BoolSetting showBackground = new BoolSetting("Background",
		"Draws a background behind the module list.", true);

	private final BoolSetting showCategoryColor = new BoolSetting(
		"Category colors", "Tints each entry with its category color.", true);

	private final EnumSetting<SortMode> sortMode = new EnumSetting<>("Sort",
		"How the module list is sorted.", SortMode.values(), SortMode.Length);

	public HudModule()
	{
		super("HUD", "Shows a list of enabled modules on screen.",
			ModuleCategory.CLIENT);
		displayName("界面");

		addSetting(showInfo);
		addSetting(showKeybinds);
		addSetting(showBackground);
		addSetting(showCategoryColor);
		addSetting(sortMode);

		subscribe(Render2DEvent.class, this::onRender2D);
	}

	public boolean showInfo()
	{
		return showInfo.isChecked();
	}

	public boolean showKeybinds()
	{
		return showKeybinds.isChecked();
	}

	public SortMode sortMode()
	{
		return sortMode.get();
	}

	private void onRender2D(Render2DEvent event)
	{
		if(NovaClient.MC.player == null)
			return;

		List<Module> modules = new ArrayList<>(ModuleManager.getAll());
		modules.removeIf(m -> !m.isEnabled() || m == this);

		if(modules.isEmpty())
			return;

		if(sortMode.get() == SortMode.Length)
			modules.sort(Comparator
				.comparingInt((Module m) -> -NovaClient.MC.font
					.width(entry(m))));
		else
			modules.sort(Comparator.comparing(Module::displayName));

		GuiGraphicsExtractor context = event.context();
		var font = NovaClient.MC.font;

		int right = context.guiWidth() - PADDING;
		int y = PADDING;

		for(Module module : modules)
		{
			String text = entry(module);
			int width = font.width(text);
			int x = right - width;

			if(showBackground.isChecked())
				context.fill(x - 2, y - 1, right + 2, y + LINE_HEIGHT - 1,
					0x80000000);

			int color = showCategoryColor.isChecked()
				? categoryColor(module.category()) : 0xFFFFFFFF;

			context.text(font, text, x, y, color);
			y += LINE_HEIGHT;
		}
	}

	private String entry(Module module)
	{
		StringBuilder sb = new StringBuilder(module.displayName());

		if(showInfo.isChecked())
		{
			String info = module.getInfoString();
			if(info != null && !info.isEmpty())
				sb.append(' ').append(info);
		}

		if(showKeybinds.isChecked() && module.hasKeybind())
			sb.append(" [").append(module.keybind()).append(']');

		return sb.toString();
	}

	private int categoryColor(ModuleCategory category)
	{
		return switch(category)
		{
			case COMBAT -> 0xFFFF6B6B;
			case MOVEMENT -> 0xFF6BE3FF;
			case RENDER -> 0xFFFFD166;
			case PLAYER -> 0xFF9B8CFF;
			case WORLD -> 0xFF7BE38A;
			case MISC -> 0xFFC9C9D6;
			case CLIENT -> GuiTheme.PRIMARY_TOP;
		};
	}

	public enum SortMode
	{
		Length, Alphabetical;
	}
}
