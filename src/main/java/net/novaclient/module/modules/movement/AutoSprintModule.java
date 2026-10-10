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
import net.novaclient.setting.BoolSetting;

/** Ported from Wurst's AutoSprintHack. */
public final class AutoSprintModule extends Module
{
	private final BoolSetting allDirections = new BoolSetting(
		"Omnidirectional Sprint",
		"Sprint in all directions, not just forward.", false);

	private final BoolSetting hungry = new BoolSetting("Hungry Sprint",
		"Sprint even on low hunger.", false);

	public AutoSprintModule()
	{
		super("AutoSprint", "Automatically sprints whenever you move.",
			ModuleCategory.MOVEMENT);
		displayName("自动疾跑");

		addSetting(allDirections);
		addSetting(hungry);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	public boolean shouldOmniSprint()
	{
		return isEnabled() && allDirections.isChecked();
	}

	public boolean shouldSprintHungry()
	{
		return isEnabled() && hungry.isChecked();
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player.horizontalCollision || player.isShiftKeyDown())
			return;

		if(player.isSwimming())
			return;

		if(player.isUnderWater() && !player.getAbilities().flying)
			return;

		if(!allDirections.isChecked()
			&& player.input.getMoveVector().y <= 0)
			return;

		if(player.input.getMoveVector().length() <= 1e-5F)
			return;

		player.setSprinting(true);
	}
}
