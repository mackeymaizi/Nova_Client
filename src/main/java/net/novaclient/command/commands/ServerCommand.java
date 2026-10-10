/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.permissions.Permissions;
import net.novaclient.command.Command;

/** {@code .server} - prints information about the current server. */
public final class ServerCommand extends Command
{
	public ServerCommand()
	{
		super("server", "Prints server information.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.executes(ctx -> {
			basicInfo();
			return SINGLE_SUCCESS;
		});

		builder.then(literal("info").executes(ctx -> {
			basicInfo();
			return SINGLE_SUCCESS;
		}));
	}

	private void basicInfo()
	{
		if(mc().hasSingleplayerServer())
		{
			info("Singleplayer");
			var server = mc().getSingleplayerServer();
			if(server != null)
				info("Version: %s", server.getServerVersion());
			return;
		}

		ServerData server = mc().getCurrentServer();

		if(server == null)
		{
			warning("Couldn't obtain any server information.");
			return;
		}

		info("IP: %s", server.ip);
		info("Name: %s", server.name);
		info("Version: %s",
			server.version != null ? server.version.getString() : "unknown");
		info("Protocol version: %d", server.protocol);
		info("Ping: %dms", server.ping);
		info("Permission level: %s", formatPerms());
	}

	private String formatPerms()
	{
		if(mc().player == null)
			return "unknown";

		var permissions = mc().player.permissions();

		if(permissions.hasPermission(Permissions.COMMANDS_OWNER))
			return "4 (Owner)";
		if(permissions.hasPermission(Permissions.COMMANDS_ADMIN))
			return "3 (Admin)";
		if(permissions.hasPermission(Permissions.COMMANDS_GAMEMASTER))
			return "2 (Gamemaster)";
		if(permissions.hasPermission(Permissions.COMMANDS_MODERATOR))
			return "1 (Moderator)";

		return "0 (No Perms)";
	}
}
