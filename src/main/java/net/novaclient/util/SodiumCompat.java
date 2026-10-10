/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.util;

import net.novaclient.NovaClient;
import net.novaclient.NovaMixinPlugin;

/**
 * Bridges to Sodium without hard-linking it.
 *
 * <p>Every method here is a no-op when Sodium is absent. The Sodium types are
 * only touched inside the guarded calls, and the whole body is wrapped in a
 * {@code Throwable} guard so a class-loading failure can never crash the game.
 */
public final class SodiumCompat
{
	private SodiumCompat()
	{}

	/** True when Sodium is loaded. */
	public static boolean isPresent()
	{
		return NovaMixinPlugin.isSodiumPresent();
	}

	/**
	 * Asks Sodium to rebuild every chunk mesh. Used when X-Ray is toggled so
	 * the change is visible immediately instead of after walking around.
	 */
	public static void reloadChunks()
	{
		if(!isPresent())
			return;

		try
		{
			if(NovaClient.MC.levelRenderer instanceof
				net.caffeinemc.mods.sodium.client.world.LevelRendererExtension extension)
				extension.sodium$getWorldRenderer().reload();
		}catch(Throwable ignored)
		{
			// Sodium is present but the API moved - not worth crashing over.
		}
	}
}
