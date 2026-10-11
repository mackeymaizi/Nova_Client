/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.client;

import java.awt.Color;

import net.novaclient.NovaClient;
import net.novaclient.gui.clickgui.ClickGuiScreen;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.ColorSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.IntSetting;

/** Nova's ClickGUI, inspired by Meteor's ClickGUI. */
public final class ClickGuiModule extends Module
{
	public static final Color PRIMARY_TOP = new Color(37, 99, 235);
	public static final Color PRIMARY_BOTTOM = new Color(78, 72, 230);

	private final DoubleSetting scale = new DoubleSetting("Scale",
		"The scale of the ClickGUI.", 1.0, 0.5, 2.0, 0.05);

	private final IntSetting opacity = new IntSetting("Opacity",
		"The opacity of the ClickGUI background.", 235, 0, 255, 5);

	private final BoolSetting shadows = new BoolSetting("Shadows",
		"Renders a drop shadow behind windows.", true);

	private final BoolSetting animate = new BoolSetting("Animation",
		"Animates the opening/closing of the ClickGUI.", true);

	private final BoolSetting chatFeedback = new BoolSetting("Chat feedback",
		"Prints a chat message when a module is toggled.", true);

	private final BoolSetting forceCollapse = new BoolSetting(
		"Force collapse",
		"Collapse every category when the screen is too small to fit them.",
		true);

	private final ColorSetting accent = new ColorSetting("Accent",
		"The accent color used for enabled modules.", PRIMARY_TOP);

	public ClickGuiModule()
	{
		super("ClickGUI", "Opens Nova's ClickGUI.", ModuleCategory.CLIENT);
		displayName("点击界面");

		addSetting(scale);
		addSetting(opacity);
		addSetting(shadows);
		addSetting(animate);
		addSetting(chatFeedback);
		addSetting(forceCollapse);
		addSetting(accent);
	}

	public double scale()
	{
		return scale.get();
	}

	public int opacity()
	{
		return opacity.get();
	}

	public boolean shadows()
	{
		return shadows.isChecked();
	}

	public boolean animate()
	{
		return animate.isChecked();
	}

	/** Whether toggling a module should print a chat message. */
	public boolean chatFeedback()
	{
		return chatFeedback.isChecked();
	}

	/** Whether to collapse everything when the screen is too small. */
	public boolean forceCollapse()
	{
		return forceCollapse.isChecked();
	}

	public Color accent()
	{
		return accent.get();
	}

	@Override
	public void onEnable()
	{
		// The screen handles the "open" state, so we just push it and
		// immediately toggle back off - the module acts as a trigger.
		NovaClient.MC.execute(() -> {
			if(NovaClient.MC.screen instanceof ClickGuiScreen)
				NovaClient.MC.setScreen(null);
			else
				NovaClient.MC.setScreen(new ClickGuiScreen());
		});

		setEnabled(false);
	}
}
