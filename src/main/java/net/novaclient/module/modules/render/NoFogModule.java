/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;

/** Ported from Wurst's NoFogHack. */
public final class NoFogModule extends Module
{
	public NoFogModule()
	{
		super("NoFog", "Removes the fog that limits your view distance.",
			ModuleCategory.RENDER);
		displayName("无雾");
	}
}
