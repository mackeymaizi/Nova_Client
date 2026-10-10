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

	public PacketLoggerModule()
	{
		super("PacketLogger", "Logs packets to the chat.", ModuleCategory.MISC);
		displayName("数据包记录");

		addSetting(outgoing);
		addSetting(incoming);

		subscribe(PacketSendEvent.class, this::onSend);
		subscribe(PacketReceiveEvent.class, this::onReceive);
	}

	private void onSend(PacketSendEvent event)
	{
		if(!outgoing.isChecked())
			return;

		ChatUtils.send("\u00a78[\u00a7c>\u00a78] \u00a77"
			+ event.packet().getClass().getSimpleName());
	}

	private void onReceive(PacketReceiveEvent event)
	{
		if(!incoming.isChecked())
			return;

		ChatUtils.send("\u00a78[\u00a7a<\u00a78] \u00a77"
			+ event.packet().getClass().getSimpleName());
	}
}
