package net.novaclient.event.events;

import net.minecraft.client.gui.GuiGraphics;

import net.novaclient.event.Event;

public final class Render2DEvent extends Event
{
	private final GuiGraphics context;
	private final float tickDelta;

	public Render2DEvent(GuiGraphics context, float tickDelta)
	{
		this.context = context;
		this.tickDelta = tickDelta;
	}

	public GuiGraphics context()
	{
		return context;
	}

	public float tickDelta()
	{
		return tickDelta;
	}
}
