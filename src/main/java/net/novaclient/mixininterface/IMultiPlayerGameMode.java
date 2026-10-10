/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixininterface;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Re-exposes the inventory actions that Mojang moved into
 * {@code handleContainerInput()} in Minecraft 26.2.
 */
public interface IMultiPlayerGameMode
{
	public void nova_windowClick_PICKUP(int slot);

	public void nova_windowClick_QUICK_MOVE(int slot);

	public void nova_windowClick_THROW(int slot);

	public void nova_windowClick_SWAP(int from, int to);

	public void nova_rightClickItem();

	public void nova_rightClickBlock(BlockPos pos, Direction side,
		Vec3 hitVec);

	public void nova_sendPlayerActionC2SPacket(
		ServerboundPlayerActionPacket.Action action, BlockPos blockPos,
		Direction direction);

	public void nova_sendPlayerInteractBlockPacket(InteractionHand hand,
		BlockHitResult blockHitResult);
}
