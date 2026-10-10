package net.novaclient.event.events;

import net.minecraft.world.entity.Entity;

import net.novaclient.event.Event;

public final class InvisibleToEvent extends Event
{
	private final Entity entity;
	private boolean invisible;

	public InvisibleToEvent(Entity entity, boolean invisible)
	{
		this.entity = entity;
		this.invisible = invisible;
	}

	public Entity entity()
	{
		return entity;
	}

	public boolean invisible()
	{
		return invisible;
	}

	public void setInvisible(boolean invisible)
	{
		this.invisible = invisible;
	}
}
