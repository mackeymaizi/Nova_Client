/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.player;

import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.DoubleSetting;

/** Ported from Wurst's ReachHack. */
public final class ReachModule extends Module
{
	private final DoubleSetting range =
		new DoubleSetting("Range", 6, 1, 10, 0.05);

	public ReachModule()
	{
		super("Reach", "Increases your reach distance.",
			ModuleCategory.PLAYER);
		displayName("加长手");

		addSetting(range);
	}

	public double getReachDistance()
	{
		return range.get();
	}

	@Override
	public String getInfoString()
	{
		return String.format("%.2f", range.get().doubleValue());
	}
}
