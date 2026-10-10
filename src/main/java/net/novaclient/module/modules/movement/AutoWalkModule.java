/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.EnumSetting;

/**
 * Ported from Wurst's AutoWalkHack. Keeps the forward key held down.
 */
public final class AutoWalkModule extends Module
{
	private final EnumSetting<Direction> direction = new EnumSetting<>(
		"Direction", "Which way to walk.", Direction.values(), Direction.Forward);

	public AutoWalkModule()
	{
		super("AutoWalk", "Automatically walks forwards.", ModuleCategory.MOVEMENT);
		displayName("自动前进");

		addSetting(direction);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	@Override
	public void onDisable()
	{
		var options = NovaClient.MC.options;
		options.keyUp.setDown(false);
		options.keyDown.setDown(false);
		options.keyLeft.setDown(false);
		options.keyRight.setDown(false);
	}

	private void onUpdate()
	{
		var options = NovaClient.MC.options;

		switch(direction.get())
		{
			case Forward -> options.keyUp.setDown(true);
			case Backward -> options.keyDown.setDown(true);
			case Left -> options.keyLeft.setDown(true);
			case Right -> options.keyRight.setDown(true);
		}
	}

	public enum Direction
	{
		Forward, Backward, Left, Right;
	}
}
