package net.novaclient.util;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;

public enum BlockUtils
{
	;

	public static BlockState getState(BlockPos pos)
	{
		return NovaClient.MC.level.getBlockState(pos);
	}

	public static boolean canBeClicked(BlockPos pos)
	{
		return getState(pos).getShape(NovaClient.MC.level, pos).isEmpty()
			|| !getState(pos).isAir();
	}

	/** True when the box only overlaps liquids (used by Jesus). */
	public static boolean isOverLiquid(AABB box)
	{
		BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
		BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);

		boolean foundLiquid = false;
		boolean foundSolid = false;

		for(BlockPos pos : BlockPos.betweenClosed(min, max))
		{
			BlockState state = getState(pos);

			if(state.getBlock() instanceof LiquidBlock)
				foundLiquid = true;
			else if(!state.isAir())
				foundSolid = true;
		}

		return foundLiquid && !foundSolid;
	}

	/** Raycasts between two points, used by Trajectories. */
	public static net.minecraft.world.phys.BlockHitResult raycast(Vec3 start,
		Vec3 end, net.minecraft.world.level.ClipContext.Fluid fluidHandling)
	{
		net.minecraft.world.level.ClipContext context =
			new net.minecraft.world.level.ClipContext(start, end,
				net.minecraft.world.level.ClipContext.Block.COLLIDER,
				fluidHandling, NovaClient.MC.player);

		return NovaClient.MC.level.clip(context);
	}

	/** True when the block at {@code pos} cannot be broken (bedrock etc.). */
	public static boolean isUnbreakable(BlockPos pos)
	{
		float hardness = getState(pos).getDestroySpeed(NovaClient.MC.level, pos);
		return hardness < 0;
	}

	/**
	 * Breaks a block instantly using the default {@link BreakMode#SPEED} mode.
	 */
	public static void breakBlock(BlockPos pos)
	{
		breakBlock(pos, BreakMode.SPEED);
	}

	/**
	 * Breaks a single block using the given {@link BreakMode}.
	 *
	 * <ul>
	 * <li>{@code NORMAL} - vanilla mining, one {@code continueDestroyBlock}
	 * call. Slowest, but the least suspicious.</li>
	 * <li>{@code LEGIT} - Wurst's NukerLegit approach: a
	 * {@code startDestroyBlock} followed by a {@code continueDestroyBlock}.
	 * Bypasses anti-cheats that only watch the stop packet.</li>
	 * <li>{@code SPEED} - sends the raw start/stop destroy packets (the same
	 * trick Wurst and Meteor use). Fastest, but the most likely to be
	 * flagged.</li>
	 * </ul>
	 */
	public static void breakBlock(BlockPos pos, BreakMode mode)
	{
		if(NovaClient.MC.gameMode == null || NovaClient.MC.level == null)
			return;

		if(getState(pos).isAir() || isUnbreakable(pos))
			return;

		Direction side = NovaClient.MC.hitResult instanceof
			net.minecraft.world.phys.BlockHitResult hit
				? hit.getDirection() : Direction.UP;

		switch(mode)
		{
			case NORMAL -> NovaClient.MC.gameMode.continueDestroyBlock(pos,
				side);

			case LEGIT ->
			{
				NovaClient.MC.gameMode.startDestroyBlock(pos, side);
				NovaClient.MC.gameMode.continueDestroyBlock(pos, side);
			}

			case SPEED ->
			{
				net.novaclient.mixininterface.IMultiPlayerGameMode gameMode =
					(net.novaclient.mixininterface.IMultiPlayerGameMode)NovaClient.MC.gameMode;

				gameMode.nova_sendPlayerActionC2SPacket(
					ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
					pos, side);
				gameMode.nova_sendPlayerActionC2SPacket(
					ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
					pos, side);

				// Let the server know the block is gone on our side too.
				NovaClient.MC.level.setBlock(pos,
					getState(pos).getFluidState().createLegacyBlock(), 0);
			}
		}
	}

	/** How {@link #breakBlock(BlockPos, BreakMode)} should remove a block. */
	public enum BreakMode
	{
		NORMAL("Normal"),
		LEGIT("Legit"),
		SPEED("Speed");

		private final String displayName;

		BreakMode(String displayName)
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

	/**
	 * Sends a START/STOP destroy packet pair for every block in the list,
	 * always using the side that faces the player. The client-side world is
	 * left untouched, so the same list can be spammed over and over - this is
	 * the trick Wurst uses in its Nuker and Kaboom hacks.
	 */
	public static void breakBlocksWithPacketSpam(List<BlockPos> blocks)
	{
		if(NovaClient.MC.player == null
			|| NovaClient.MC.player.connection == null
			|| NovaClient.MC.level == null)
			return;

		Vec3 eyesPos = RotationUtils.getEyesPos();
		net.minecraft.client.multiplayer.ClientPacketListener netHandler =
			NovaClient.MC.player.connection;

		for(BlockPos pos : blocks)
		{
			if(getState(pos).isAir() || isUnbreakable(pos))
				continue;

			Vec3 posVec = Vec3.atCenterOf(pos);
			double distanceSqPosVec = eyesPos.distanceToSqr(posVec);

			for(Direction side : Direction.values())
			{
				Vec3 hitVec = posVec.add(
					Vec3.atLowerCornerOf(side.getUnitVec3i()).scale(0.5));

				// Skip sides that point away from the player.
				if(eyesPos.distanceToSqr(hitVec) >= distanceSqPosVec)
					continue;

				netHandler.send(new ServerboundPlayerActionPacket(
					ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
					pos, side));
				netHandler.send(new ServerboundPlayerActionPacket(
					ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
					pos, side));
				break;
			}
		}
	}
}
