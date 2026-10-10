package net.novaclient.event.events;

import net.novaclient.event.Event;

public final class TickEvent extends Event
{
	private final boolean pre;

	public TickEvent(boolean pre)
	{
		this.pre = pre;
	}

	public boolean isPre()
	{
		return pre;
	}
}
