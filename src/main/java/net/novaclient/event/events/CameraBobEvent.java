package net.novaclient.event.events;

import net.novaclient.event.Event;

/** Fired when the view bobbing transformation is applied. */
public final class CameraBobEvent extends Event
{
	public static final CameraBobEvent INSTANCE = new CameraBobEvent();
}
