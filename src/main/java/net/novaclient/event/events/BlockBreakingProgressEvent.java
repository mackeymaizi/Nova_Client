package net.novaclient.event.events;

import net.minecraft.core.BlockPos;

import net.novaclient.event.Event;

public final class BlockBreakingProgressEvent extends Event
{
	private final BlockPos blockPos;

	public BlockBreakingProgressEvent(BlockPos blockPos)
	{
		this.blockPos = blockPos;
	}

	public BlockPos blockPos()
	{
		return blockPos;
	}
}
