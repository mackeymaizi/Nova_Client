/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.misc;

import net.novaclient.NovaClient;
import net.novaclient.event.events.FluidPushEvent;
import net.novaclient.event.events.IsPlayerInWaterEvent;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.mixininterface.ILocalPlayer;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;

/** Ported from Wurst's AntiWaterPushHack. */
public final class AntiWaterPushModule extends Module
{
	private final BoolSetting preventSlowdown = new BoolSetting(
		"Prevent slowdown", "Allows you to walk underwater at full speed.\n"
			+ "Some servers consider this a speedhack.",
		false);

	public AntiWaterPushModule()
	{
		super("AntiWaterPush", "Prevents water currents from pushing you"
			+ " around.", ModuleCategory.MISC);
		displayName("防水流推动");

		addSetting(preventSlowdown);

		subscribe(FluidPushEvent.class, e -> e.setPushedByFluid(false));

		subscribe(UpdateEvent.class, e -> {
			if(!preventSlowdown.isChecked())
				return;

			if(!NovaClient.MC.options.keyJump.isDown())
				return;

			if(!NovaClient.MC.player.onGround())
				return;

			if(!((ILocalPlayer)NovaClient.MC.player).nova_isInWaterBypass())
				return;

			NovaClient.MC.player.jumpFromGround();
		});

		subscribe(IsPlayerInWaterEvent.class, e -> {
			if(preventSlowdown.isChecked())
				e.setInWater(false);
		});
	}
}
