package net.novaclient.event.events;

import net.novaclient.event.Event;

/** Fired once per client tick while a player is loaded. */
public final class UpdateEvent extends Event
{
	public static final UpdateEvent INSTANCE = new UpdateEvent();
}
