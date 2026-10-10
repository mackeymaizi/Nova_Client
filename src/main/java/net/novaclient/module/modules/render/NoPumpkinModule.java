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

/** Ported from Wurst's NoPumpkinHack. */
public final class NoPumpkinModule extends Module
{
	public NoPumpkinModule()
	{
		super("NoPumpkin", "Removes the pumpkin overlay.",
			ModuleCategory.RENDER);
		displayName("无南瓜头");
	}
}
