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
import net.novaclient.setting.IntSetting;

/** Ported from Wurst's HighJumpHack. */
public final class HighJumpModule extends Module
{
	private final IntSetting height = new IntSetting("Height",
		"Jump height in blocks.\nThis gets very inaccurate at higher values.",
		6, 1, 100, 1);

	public HighJumpModule()
	{
		super("HighJump", "Lets you jump higher than normal.",
			ModuleCategory.MOVEMENT);
		displayName("高跳");

		addSetting(height);
	}

	/** Extra upward motion added to the vanilla jump power. */
	public float getAdditionalJumpMotion()
	{
		return isEnabled() ? height.get() * 0.1F : 0;
	}

	@Override
	public String getInfoString()
	{
		return String.valueOf(height.get());
	}
}
