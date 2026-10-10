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

/** Ported from Wurst's SpiderHack. Lets you climb walls. */
public final class SpiderModule extends Module
{
	public SpiderModule()
	{
		super("Spider", "Lets you climb up walls.", ModuleCategory.MOVEMENT);
		displayName("爬墙");

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player == null || !player.horizontalCollision)
			return;

		Vec3 velocity = player.getDeltaMovement();
		if(velocity.y >= 0.2)
			return;

		player.setDeltaMovement(velocity.x, 0.2, velocity.z);
	}
}
