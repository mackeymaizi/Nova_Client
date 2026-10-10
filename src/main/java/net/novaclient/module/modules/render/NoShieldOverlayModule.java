/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.DoubleSetting;

/** Ported from Wurst's NoShieldOverlayHack. */
public final class NoShieldOverlayModule extends Module
{
	private final DoubleSetting blockingOffset = new DoubleSetting(
		"Blocking offset",
		"The amount to lower the shield overlay by when blocking.", 0.5, 0, 0.8,
		0.01);

	private final DoubleSetting nonBlockingOffset = new DoubleSetting(
		"Non-blocking offset",
		"The amount to lower the shield overlay when not blocking.", 0.2, 0,
		0.5, 0.01);

	public NoShieldOverlayModule()
	{
		super("NoShieldOverlay", "Lowers the shield so you can see through it.",
			ModuleCategory.RENDER);
		displayName("无盾牌遮挡");

		addSetting(blockingOffset);
		addSetting(nonBlockingOffset);
	}

	public void adjustShieldPosition(PoseStack poseStack, boolean blocking)
	{
		if(!isEnabled())
			return;

		poseStack.translate(0,
			-(blocking ? blockingOffset.get() : nonBlockingOffset.get()), 0);
	}
}
