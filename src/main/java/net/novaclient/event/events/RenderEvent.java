package net.novaclient.event.events;

import com.mojang.blaze3d.vertex.PoseStack;

import net.novaclient.event.Event;

public final class RenderEvent extends Event
{
	private final PoseStack poseStack;
	private final float tickDelta;

	public RenderEvent(PoseStack poseStack, float tickDelta)
	{
		this.poseStack = poseStack;
		this.tickDelta = tickDelta;
	}

	public PoseStack poseStack()
	{
		return poseStack;
	}

	public float tickDelta()
	{
		return tickDelta;
	}
}
