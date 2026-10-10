/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.util;

/**
 * Placeholder for the Sodium bridge used by the 26.2 branch.
 *
 * <p>Minecraft 1.21.11 uses a completely different Sodium version (0.6.x, which
 * lives in a different package and has a different chunk renderer), so the
 * Sodium integration is not ported yet. Everything here degrades to a no-op and
 * X-Ray simply uses Minecraft's own chunk renderer.
 */
public final class SodiumCompat
{
	private SodiumCompat()
	{}

	/** Always false on 1.21.11 - the Sodium bridge is not ported yet. */
	public static boolean isPresent()
	{
		return false;
	}

	/** No-op. The vanilla chunk re-mesh is done by the caller. */
	public static void reloadChunks()
	{}
}
