/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.misc;

import net.novaclient.event.events.PacketReceiveEvent;
import net.novaclient.event.events.PacketSendEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.util.ChatUtils;

/** Ported from Meteor's PacketLogger. Prints every packet to chat. */
public final class PacketLoggerModule extends Module
{
	private final BoolSetting outgoing = new BoolSetting("Outgoing",
		"Log packets you send.", true);

	private final BoolSetting incoming = new BoolSetting("Incoming",
		"Log packets you receive.", false);

	private final BoolSetting showTimestamp = new BoolSetting("Show timestamp",
		"Prefix each line with the current time.", false);

	private final BoolSetting showPacketData = new BoolSetting("Show packet data",
		"Include the packet's toString() payload.", true);

	private final BoolSetting showCount = new BoolSetting("Show total count",
		"Show the running number of logged packets.", false);

	private final BoolSetting logToChat = new BoolSetting("Log to chat",
		"Print the packets to the chat box.", true);

	private int count;

	public PacketLoggerModule()
	{
		super("PacketLogger", "Logs packets to the chat.", ModuleCategory.MISC);
		displayName("数据包记录");

		addSetting(outgoing);
		addSetting(incoming);
		addSetting(showTimestamp);
		addSetting(showPacketData);
		addSetting(showCount);
		addSetting(logToChat);

		subscribe(PacketSendEvent.class, this::onSend);
		subscribe(PacketReceiveEvent.class, this::onReceive);
	}

	private void onSend(PacketSendEvent event)
	{
		if(!outgoing.isChecked())
			return;

		log("\u00a78[\u00a7c>\u00a78] \u00a77", event.packet());
	}

	private void onReceive(PacketReceiveEvent event)
	{
		if(!incoming.isChecked())
			return;

		log("\u00a78[\u00a7a<\u00a78] \u00a77", event.packet());
	}

	private void log(String prefix, Object packet)
	{
		if(!logToChat.isChecked())
			return;

		count++;

		StringBuilder sb = new StringBuilder();

		if(showTimestamp.isChecked())
			sb.append("\u00a78[")
				.append(new java.text.SimpleDateFormat("HH:mm:ss")
					.format(new java.util.Date()))
				.append("] ");

		sb.append(prefix).append(packet.getClass().getSimpleName());

		if(showCount.isChecked())
			sb.append(" \u00a78#").append(count);

		if(showPacketData.isChecked())
			sb.append(" \u00a78").append(packet);

		ChatUtils.send(sb.toString());
	}
}
