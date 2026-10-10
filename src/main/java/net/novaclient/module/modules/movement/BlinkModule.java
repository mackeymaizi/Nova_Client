/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import java.util.ArrayDeque;
import java.util.Deque;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.novaclient.NovaClient;
import net.novaclient.event.events.PacketSendEvent;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.IntSetting;

/**
 * Ported from Wurst's BlinkHack. Suspends your movement packets so that other
 * players see you frozen in place, then flushes them all at once.
 */
public final class BlinkModule extends Module
{
	private final IntSetting limit = new IntSetting("Limit",
		"Automatically restarts Blink once this many packets are suspended."
			+ " 0 = no limit.",
		0, 0, 500, 1);

	private final Deque<Packet<?>> packets = new ArrayDeque<>();

	public BlinkModule()
	{
		super("Blink", "Suspends your movement packets, then sends them at once.",
			ModuleCategory.MOVEMENT);
		displayName("延迟发包");

		addSetting(limit);

		subscribe(PacketSendEvent.class, this::onSend);
		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	@Override
	public void onDisable()
	{
		var player = NovaClient.MC.player;
		if(player != null)
			packets.forEach(p -> player.connection.send(p));

		packets.clear();
	}

	@Override
	public String getInfoString()
	{
		int max = limit.get();
		return max == 0 ? String.valueOf(packets.size())
			: packets.size() + "/" + max;
	}

	private void onUpdate()
	{
		int max = limit.get();

		if(max <= 0 || packets.size() < max)
			return;

		setEnabled(false);
		setEnabled(true);
	}

	private void onSend(PacketSendEvent event)
	{
		if(!(event.packet() instanceof ServerboundMovePlayerPacket))
			return;

		event.cancel();
		packets.add(event.packet());
	}
}
