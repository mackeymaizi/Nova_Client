/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import net.novaclient.event.events.CameraBobEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;

/** Ported from Wurst's AntiWobbleHack. */
public final class AntiWobbleModule extends Module
{
	public AntiWobbleModule()
	{
		super("AntiWobble", "Removes the nausea and portal wobble effects,"
			+ " as well as view bobbing.", ModuleCategory.RENDER);
		displayName("防摇晃");

		subscribe(CameraBobEvent.class, e -> e.cancel());
	}
}
