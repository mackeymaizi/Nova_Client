/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.novaclient.command.Command;
import net.novaclient.command.CommandManager;

/** {@code .commands} - lists every available command. */
public final class CommandsCommand extends Command
{
	public CommandsCommand()
	{
		super("commands", "Lists all available commands.", "cmds");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.executes(ctx -> {
			info("Commands (%d):", CommandManager.COMMANDS.size());

			for(Command command : CommandManager.COMMANDS)
				ChatUtils_line(ChatFormatting.WHITE + " " + command
					+ ChatFormatting.GRAY + " - " + command.getDescription());

			return SINGLE_SUCCESS;
		});
	}

	private void ChatUtils_line(String message)
	{
		net.novaclient.util.ChatUtils.send(message);
	}
}
