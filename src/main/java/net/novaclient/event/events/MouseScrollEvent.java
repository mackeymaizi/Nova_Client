package net.novaclient.event.events;

import net.novaclient.event.Event;

public final class MouseScrollEvent extends Event
{
	private final double horizontal;
	private final double vertical;

	public MouseScrollEvent(double horizontal, double vertical)
	{
		this.horizontal = horizontal;
		this.vertical = vertical;
	}

	public double horizontal()
	{
		return horizontal;
	}

	public double vertical()
	{
		return vertical;
	}
}
