/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.misc;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.gui.clickgui.ClickGuiScreen;
import net.novaclient.mixininterface.IKeyMapping;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;

/** Ported from Wurst's InvWalkHack. */
public final class InvWalkModule extends Module
{
	private final BoolSetting allowClickGUI = new BoolSetting("Allow ClickGUI",
		"Allows you to walk while Nova's ClickGUI is open.", true);

	private final BoolSetting allowOther = new BoolSetting(
		"Allow other screens",
		"Allows you to walk while any other container screen is open.", true);

	private final BoolSetting allowSneak =
		new BoolSetting("Allow sneak key", true);

	private final BoolSetting allowSprint =
		new BoolSetting("Allow sprint key", true);

	private final BoolSetting allowJump =
		new BoolSetting("Allow jump key", true);

	public InvWalkModule()
	{
		super("InvWalk", "Allows you to walk while a GUI is open.",
			ModuleCategory.MOVEMENT);
		displayName("背包行走");

		addSetting(allowClickGUI);
		addSetting(allowOther);
		addSetting(allowSneak);
		addSetting(allowSprint);
		addSetting(allowJump);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	private void onUpdate()
	{
		Screen screen = NovaClient.MC.screen;
		if(screen == null || !isAllowedScreen(screen))
			return;

		List<KeyMapping> keys = new ArrayList<>(
			List.of(NovaClient.MC.options.keyUp, NovaClient.MC.options.keyDown,
				NovaClient.MC.options.keyLeft,
				NovaClient.MC.options.keyRight));

		if(allowSneak.isChecked())
			keys.add(NovaClient.MC.options.keyShift);

		if(allowSprint.isChecked())
			keys.add(NovaClient.MC.options.keySprint);

		if(allowJump.isChecked())
			keys.add(NovaClient.MC.options.keyJump);

		for(KeyMapping key : keys)
			((IKeyMapping)key).nova_resetPressedState();
	}

	private boolean isAllowedScreen(Screen screen)
	{
		if(screen instanceof InventoryScreen
			|| screen instanceof CreativeModeInventoryScreen)
			return true;

		if(allowClickGUI.isChecked() && screen instanceof ClickGuiScreen)
			return true;

		if(allowOther.isChecked() && screen instanceof AbstractContainerScreen
			&& !hasTextBox(screen))
			return true;

		return false;
	}

	private boolean hasTextBox(Screen screen)
	{
		return screen.children().stream().anyMatch(EditBox.class::isInstance);
	}
}
