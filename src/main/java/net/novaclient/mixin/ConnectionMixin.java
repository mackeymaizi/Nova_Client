/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.novaclient.event.EventBus;
import net.novaclient.event.events.PacketReceiveEvent;
import net.novaclient.event.events.PacketSendEvent;

@Mixin(Connection.class)
public abstract class ConnectionMixin
{
	private final ConcurrentLinkedQueue<PacketSendEvent> nova_events =
		new ConcurrentLinkedQueue<>();

	@Inject(
		method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V",
			ordinal = 0),
		cancellable = true)
	private void nova_onChannelRead0(ChannelHandlerContext context,
		Packet<?> packet, CallbackInfo ci)
	{
		PacketReceiveEvent event = new PacketReceiveEvent(packet);
		EventBus.post(event);

		if(event.isCancelled())
			ci.cancel();
	}

	// Targets the two-argument "send" method.

	@ModifyVariable(
		method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V",
		at = @At("HEAD"))
	public Packet<?> nova_modifyPacket(Packet<?> packet)
	{
		PacketSendEvent event = new PacketSendEvent(packet);
		nova_events.add(event);
		EventBus.post(event);
		return event.packet();
	}

	@Inject(
		method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V",
		at = @At("HEAD"),
		cancellable = true)
	private void nova_onSend(Packet<?> packet,
		io.netty.channel.ChannelFutureListener callback, CallbackInfo ci)
	{
		PacketSendEvent event = nova_getEvent(packet);
		if(event == null)
			return;

		if(event.isCancelled())
			ci.cancel();

		nova_events.remove(event);
	}

	private PacketSendEvent nova_getEvent(Packet<?> packet)
	{
		for(PacketSendEvent event : nova_events)
			if(event.packet() == packet)
				return event;

		return null;
	}
}
