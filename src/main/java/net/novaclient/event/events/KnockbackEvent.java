package net.novaclient.event.events;

import net.minecraft.world.phys.Vec3;

import net.novaclient.event.Event;

public final class KnockbackEvent extends Event
{
	private Vec3 knockback;

	public KnockbackEvent(Vec3 knockback)
	{
		this.knockback = knockback;
	}

	public Vec3 knockback()
	{
		return knockback;
	}

	public void setKnockback(Vec3 knockback)
	{
		this.knockback = knockback;
	}
}
