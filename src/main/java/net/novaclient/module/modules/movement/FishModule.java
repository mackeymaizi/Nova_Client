/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;

/** Ported from Wurst's FishHack. */
public final class FishModule extends Module
{
	public FishModule()
	{
		super("Fish", "Automatically swims upwards in water.",
			ModuleCategory.MOVEMENT);
		displayName("自动钓鱼");

		subscribe(UpdateEvent.class, e -> {
			var player = NovaClient.MC.player;

			if(!player.isInWater() || player.isShiftKeyDown())
				return;

			Vec3 velocity = player.getDeltaMovement();
			player.setDeltaMovement(velocity.x, velocity.y + 0.005,
				velocity.z);
		});
	}
}
