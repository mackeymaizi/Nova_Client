/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.util;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

/**
 * Helpers for inspecting outgoing packets.
 *
 * <p>MC 26.2 split entity attacks into their own {@code ServerboundAttackPacket};
 * 1.21.11 still folds them into {@code ServerboundInteractPacket}, so we have to
 * dispatch the packet to find out which kind of interaction it is.
 */
public enum PacketUtils
{
	;

	/** True when the interact packet is a left-click attack. */
	public static boolean isAttack(ServerboundInteractPacket packet)
	{
		boolean[] attack = {false};

		packet.dispatch(new ServerboundInteractPacket.Handler()
		{
			@Override
			public void onInteraction(InteractionHand hand)
			{}

			@Override
			public void onInteraction(InteractionHand hand, Vec3 location)
			{}

			@Override
			public void onAttack()
			{
				attack[0] = true;
			}
		});

		return attack[0];
	}
}
