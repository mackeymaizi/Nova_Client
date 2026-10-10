package net.novaclient.event.events;

import net.minecraft.network.protocol.Packet;

import net.novaclient.event.Event;

public final class PacketReceiveEvent extends Event
{
	private final Packet<?> packet;

	public PacketReceiveEvent(Packet<?> packet)
	{
		this.packet = packet;
	}

	public Packet<?> packet()
	{
		return packet;
	}
}
