/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;

/** Ported from Wurst's NoSlowdownHack. */
public final class NoSlowdownModule extends Module
{
	public NoSlowdownModule()
	{
		super("NoSlowdown", "Removes the slowdown caused by blocks, using"
			+ " items and sneaking.", ModuleCategory.MOVEMENT);
		displayName("无视减速");
	}
}
