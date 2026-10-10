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

/** Ported from Wurst's CameraNoClipHack. */
public final class CameraNoClipModule extends Module
{
	public CameraNoClipModule()
	{
		super("CameraNoClip", "Allows the camera to clip through blocks in"
			+ " third person view.", ModuleCategory.RENDER);
		displayName("相机穿墙");
	}
}
