/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.misc;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.novaclient.NovaClient;
import net.novaclient.event.events.TickEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.IntSetting;
import net.novaclient.util.ChatUtils;

/**
 * Ported from Wurst's AutoReconnectHack. Reconnects to the last server after
 * you get disconnected.
 */
public final class AutoReconnectModule extends Module
{
	private final IntSetting delay = new IntSetting("Delay",
		"Seconds to wait before reconnecting.", 5, 0, 60, 1);

	private final BoolSetting notify = new BoolSetting("Notify",
		"Show a countdown in chat.", true);

	private int countdown = -1;

	public AutoReconnectModule()
	{
		super("AutoReconnect", "Reconnects you after getting disconnected.",
			ModuleCategory.MISC);
		displayName("自动重连");

		addSetting(delay);
		addSetting(notify);

		subscribe(TickEvent.class, this::onTick);
	}

	/** Called by the Gui mixin when a disconnect screen appears. */
	public void scheduleReconnect()
	{
		if(!isEnabled())
			return;

		countdown = delay.get() * 20;
	}

	@Override
	public void onDisable()
	{
		countdown = -1;
	}

	private void onTick(TickEvent event)
	{
		if(!event.isPre() || countdown < 0)
			return;

		countdown--;

		if(countdown % 20 == 0 && countdown > 0 && notify.isChecked())
			ChatUtils.nova("Reconnecting in " + (countdown / 20) + "s...");

		if(countdown > 0)
			return;

		countdown = -1;
		reconnect();
	}

	private void reconnect()
	{
		ServerData server = NovaClient.MC.getCurrentServer();

		if(server == null)
		{
			ChatUtils.error("No server to reconnect to.");
			return;
		}

		ChatUtils.nova("Reconnecting to " + server.ip + "...");

		net.minecraft.client.gui.screens.ConnectScreen.startConnecting(
			null, NovaClient.MC, ServerAddress.parseString(server.ip), server,
			false, null);
	}
}
