package net.novaclient.event.events;

import net.novaclient.event.Event;

public final class KeyPressEvent extends Event
{
	private final long window;
	private final int key;
	private final int action;
	private final int modifiers;

	public KeyPressEvent(long window, int key, int action, int modifiers)
	{
		this.window = window;
		this.key = key;
		this.action = action;
		this.modifiers = modifiers;
	}

	public long window()
	{
		return window;
	}

	public int key()
	{
		return key;
	}

	public int action()
	{
		return action;
	}

	public int modifiers()
	{
		return modifiers;
	}
}
