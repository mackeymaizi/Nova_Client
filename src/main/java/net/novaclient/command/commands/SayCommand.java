/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.novaclient.command.Command;

/** {@code .say <message>} - sends a chat message. */
public final class SayCommand extends Command
{
	public SayCommand()
	{
		super("say", "Sends a chat message.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(argument("message", StringArgumentType.greedyString())
			.executes(ctx -> {
				String message = StringArgumentType.getString(ctx, "message");

				if(mc().player == null)
					return SINGLE_SUCCESS;

				if(message.startsWith("/"))
					mc().player.connection.sendCommand(message.substring(1));
				else
					mc().player.connection.sendChat(message);

				return SINGLE_SUCCESS;
			}));
	}
}
