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
import net.novaclient.setting.DoubleSetting;

/** Ported from Wurst's JetpackHack. */
public final class JetpackModule extends Module
{
	private final DoubleSetting power = new DoubleSetting("Power",
		"How much upward motion to apply while holding jump.", 0.42, 0.05, 2,
		0.01);

	public JetpackModule()
	{
		super("Jetpack", "Lets you fly by holding the jump key.",
			ModuleCategory.MOVEMENT);
		displayName("喷气背包");

		addSetting(power);

		subscribe(UpdateEvent.class, e -> {
			if(NovaClient.MC.options.keyJump.isDown())
				NovaClient.MC.player.setDeltaMovement(
					NovaClient.MC.player.getDeltaMovement().x, power.get(),
					NovaClient.MC.player.getDeltaMovement().z);
		});
	}
}
