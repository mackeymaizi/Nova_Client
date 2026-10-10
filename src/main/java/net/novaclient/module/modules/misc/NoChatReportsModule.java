/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.misc;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.SignedMessageChain;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;

/** Ported from Wurst's NoChatReportsOtf. */
public final class NoChatReportsModule extends Module
{
	private final BoolSetting disableSignatures = new BoolSetting(
		"Disable signatures",
		"Disables chat message signatures, so your messages can't be"
			+ " reported.",
		true);

	public NoChatReportsModule()
	{
		super("NoChatReports", "Prevents your chat messages from being"
			+ " reportable to Mojang.", ModuleCategory.MISC);
		displayName("无聊天举报");

		addSetting(disableSignatures);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	public boolean isActive()
	{
		if(!isEnabled() || !disableSignatures.isChecked())
			return false;

		return NovaClient.MC.getConnection() != null;
	}

	private void onUpdate()
	{
		ClientPacketListener connection = NovaClient.MC.getConnection();
		if(connection == null)
			return;

		if(isActive())
		{
			connection.chatSession = null;
			connection.signedMessageEncoder =
				SignedMessageChain.Encoder.UNSIGNED;
		}
	}

	@Override
	public void onDisable()
	{
		// Re-enabling signatures requires rejoining the server, so there is
		// nothing to restore here.
	}
}
