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
import net.minecraft.network.chat.Component;
import net.novaclient.command.Command;

/** {@code .disconnect [reason]} - disconnects from the server. */
public final class DisconnectCommand extends Command
{
	public DisconnectCommand()
	{
		super("disconnect", "Disconnects from the server.", "dc");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.executes(ctx -> disconnect("Disconnected by user."));

		builder.then(argument("reason",
			com.mojang.brigadier.arguments.StringArgumentType.greedyString())
			.executes(ctx -> disconnect(ctx.getArgument("reason",
				String.class))));
	}

	private int disconnect(String reason)
	{
		mc().getConnection().getConnection()
			.disconnect(Component.literal(reason));
		return SINGLE_SUCCESS;
	}
}
