/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.novaclient.NovaClient;
import net.novaclient.command.Command;
import net.novaclient.util.ChatUtils;

/** {@code .profiles} - lists saved config profiles. */
public final class ProfilesCommand extends Command
{
	public ProfilesCommand()
	{
		super("profiles", "Lists saved config profiles.", "profile");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.executes(ctx -> {
			List<String> profiles = NovaClient.INSTANCE.getConfig()
				.listProfiles();

			if(profiles.isEmpty())
			{
				warning("No profiles found.");
				return SINGLE_SUCCESS;
			}

			info("Profiles (%d):", profiles.size());
			for(String profile : profiles)
				ChatUtils.send(" " + profile);

			return SINGLE_SUCCESS;
		});
	}
}
