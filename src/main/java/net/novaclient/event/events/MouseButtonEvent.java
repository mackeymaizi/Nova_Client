package net.novaclient.event.events;

import net.novaclient.event.Event;

public final class MouseButtonEvent extends Event
{
	private final int button;
	private final int action;
	private final int modifiers;

	public MouseButtonEvent(int button, int action, int modifiers)
	{
		this.button = button;
		this.action = action;
		this.modifiers = modifiers;
	}

	public int button()
	{
		return button;
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
