package net.novaclient.event.events;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.novaclient.event.Event;

public final class Render2DEvent extends Event
{
	private final GuiGraphicsExtractor context;
	private final float tickDelta;

	public Render2DEvent(GuiGraphicsExtractor context, float tickDelta)
	{
		this.context = context;
		this.tickDelta = tickDelta;
	}

	public GuiGraphicsExtractor context()
	{
		return context;
	}

	public float tickDelta()
	{
		return tickDelta;
	}
}
