package net.novaclient.event.events;

import net.novaclient.event.Event;

/** Fired to decide whether the player should be pushed by fluids. */
public final class FluidPushEvent extends Event
{
	private boolean pushedByFluid = true;

	public boolean pushedByFluid()
	{
		return pushedByFluid;
	}

	public void setPushedByFluid(boolean pushedByFluid)
	{
		this.pushedByFluid = pushedByFluid;
	}
}
