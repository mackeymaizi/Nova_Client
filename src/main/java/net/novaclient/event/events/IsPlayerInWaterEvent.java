package net.novaclient.event.events;

import net.novaclient.event.Event;

public final class IsPlayerInWaterEvent extends Event
{
	private boolean inWater;

	public IsPlayerInWaterEvent(boolean inWater)
	{
		this.inWater = inWater;
	}

	public boolean inWater()
	{
		return inWater;
	}

	public void setInWater(boolean inWater)
	{
		this.inWater = inWater;
	}
}
