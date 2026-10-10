/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.mixininterface.IMultiPlayerGameMode;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.EnumSetting;

/** Ported from Wurst's ScaffoldWalkHack. */
public final class ScaffoldModule extends Module
{
	private final EnumSetting<PlaceMode> placeMode = new EnumSetting<>(
		"Place mode",
		"Normal: client-side useItemOn (predicts the placement). "
			+ "Speed: sends the interact packet directly, which works on "
			+ "servers that reject the normal path.",
		PlaceMode.values(), PlaceMode.NORMAL);

	private final BoolSetting swing = new BoolSetting("Swing hand",
		"Swings your hand while placing.", true);

	private final BoolSetting switchBack = new BoolSetting("Switch back",
		"Switches back to your previous slot after placing.", true);

	public ScaffoldModule()
	{
		super("Scaffold", "Places blocks under you as you walk.",
			ModuleCategory.MOVEMENT);
		displayName("自动搭桥");

		addSetting(placeMode);
		addSetting(swing);
		addSetting(switchBack);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	@Override
	public String getInfoString()
	{
		return placeMode.get().displayName();
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player == null || NovaClient.MC.level == null)
			return;

		BlockPos below = BlockPos.containing(player.position()).below();

		if(!NovaClient.MC.level.getBlockState(below).canBeReplaced())
			return;

		int slot = findBlockSlot();
		if(slot == -1)
			return;

		var inventory = player.getInventory();
		int oldSlot = inventory.getSelectedSlot();
		inventory.setSelectedSlot(slot);

		placeOn(below);

		if(switchBack.isChecked())
			inventory.setSelectedSlot(oldSlot);
	}

	private int findBlockSlot()
	{
		var inventory = NovaClient.MC.player.getInventory();

		for(int i = 0; i < 9; i++)
		{
			ItemStack stack = inventory.getItem(i);
			if(stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem))
				continue;

			var state = blockItem.getBlock().defaultBlockState();
			if(!state.isCollisionShapeFullBlock(NovaClient.MC.level,
				BlockPos.ZERO))
				continue;

			if(blockItem.getBlock() instanceof FallingBlock
				&& FallingBlock.isFree(NovaClient.MC.level
					.getBlockState(BlockPos.containing(
						NovaClient.MC.player.position()).below(2))))
				continue;

			return i;
		}

		return -1;
	}

	private boolean placeOn(BlockPos pos)
	{
		var player = NovaClient.MC.player;

		for(Direction side : Direction.values())
		{
			BlockPos neighbor = pos.relative(side);
			var neighborState = NovaClient.MC.level.getBlockState(neighbor);

			if(neighborState.canBeReplaced()
				|| neighborState.getCollisionShape(NovaClient.MC.level, neighbor)
					.isEmpty())
				continue;

			BlockHitResult hit = new BlockHitResult(
				Vec3.atCenterOf(neighbor).add(
					Vec3.atLowerCornerOf(side.getUnitVec3i()).scale(0.5)),
				side.getOpposite(), neighbor, false);

			if(placeMode.get() == PlaceMode.SPEED)
			{
				((IMultiPlayerGameMode)NovaClient.MC.gameMode)
					.nova_sendPlayerInteractBlockPacket(
						InteractionHand.MAIN_HAND, hit);

				if(swing.isChecked())
					player.swing(InteractionHand.MAIN_HAND);

				return true;
			}

			var result = NovaClient.MC.gameMode.useItemOn(player,
				InteractionHand.MAIN_HAND, hit);

			if(result.consumesAction())
			{
				if(swing.isChecked())
					player.swing(InteractionHand.MAIN_HAND);

				return true;
			}
		}

		return false;
	}

	/** How Scaffold should send the block placement. */
	public enum PlaceMode
	{
		NORMAL("Normal"),
		SPEED("Speed");

		private final String displayName;

		PlaceMode(String displayName)
		{
			this.displayName = displayName;
		}

		public String displayName()
		{
			return displayName;
		}

		@Override
		public String toString()
		{
			return displayName;
		}
	}
}
