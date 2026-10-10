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

/** Ported from Wurst's NoWebHack. */
public final class NoWebModule extends Module
{
	public NoWebModule()
	{
		super("NoWeb", "Prevents cobwebs from slowing you down.",
			ModuleCategory.MOVEMENT);
		displayName("无视蛛网");

		subscribe(UpdateEvent.class,
			e -> NovaClient.MC.player.stuckSpeedMultiplier = Vec3.ZERO);
	}
}
