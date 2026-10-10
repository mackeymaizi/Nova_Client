package net.novaclient.event.events;

import net.minecraft.world.entity.Entity;

import net.novaclient.event.Event;

public final class EntityPushEvent extends Event
{
	private final Entity entity;

	public EntityPushEvent(Entity entity)
	{
		this.entity = entity;
	}

	public Entity entity()
	{
		return entity;
	}
}
