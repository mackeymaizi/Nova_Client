package net.novaclient.event.events;

import net.minecraft.network.protocol.Packet;

import net.novaclient.event.Event;

public final class PacketSendEvent extends Event
{
	private Packet<?> packet;

	public PacketSendEvent(Packet<?> packet)
	{
		this.packet = packet;
	}

	public Packet<?> packet()
	{
		return packet;
	}

	public void setPacket(Packet<?> packet)
	{
		this.packet = packet;
	}
}
