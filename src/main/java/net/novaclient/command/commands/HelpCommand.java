/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import java.util.Map;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.novaclient.command.Command;
import net.novaclient.command.CommandManager;
import net.novaclient.util.ChatUtils;

/** {@code .help [command]} - shows what a command does. */
public final class HelpCommand extends Command
{
	public HelpCommand()
	{
		super("help", "Shows you what a command does.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(argument("command", com.mojang.brigadier.arguments.StringArgumentType.word())
			.suggests((ctx, b) -> net.minecraft.commands.SharedSuggestionProvider
				.suggest(CommandManager.COMMANDS.stream().map(Command::getName), b))
			.executes(ctx -> {
				String name = ctx.getArgument("command", String.class);
				Command cmd = CommandManager.get(name);
				if(cmd == null)
				{
					error("Command \"%s\" doesn't exist.", name);
					return SINGLE_SUCCESS;
				}
				showHelp(cmd);
				return SINGLE_SUCCESS;
			}));

		builder.executes(ctx -> {
			showHelp(this);
			return SINGLE_SUCCESS;
		});
	}

	private void showHelp(Command cmd)
	{
		MutableComponent msg = Component.literal("");
		msg.append(Component.literal("Help for ").withStyle(ChatFormatting.GRAY)
			.append(Component.literal(cmd.getName())
				.withStyle(ChatFormatting.YELLOW)));
		msg.append(Component.literal("\n ")).append(
			Component.literal("Description: ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(cmd.getDescription())
					.withStyle(ChatFormatting.WHITE)));

		if(!cmd.getAliases().isEmpty())
		{
			msg.append(Component.literal("\n "))
				.append(Component.literal("Aliases: ")
					.withStyle(ChatFormatting.GRAY));
			msg.append(Component.literal(String.join(", ", cmd.getAliases()))
				.withStyle(ChatFormatting.AQUA));
		}

		msg.append(getUsageText(cmd));
		ChatUtils.send(msg.getString());
	}

	private MutableComponent getUsageText(Command cmd)
	{
		ClientSuggestionProvider source =
			mc().getConnection().getSuggestionsProvider();
		CommandNode<ClientSuggestionProvider> root =
			CommandManager.DISPATCHER.getRoot();
		CommandNode<ClientSuggestionProvider> node =
			root.getChild(cmd.getName());

		MutableComponent usagesText = Component.literal("");

		if(node != null)
		{
			Map<CommandNode<ClientSuggestionProvider>, String> usages =
				CommandManager.DISPATCHER.getSmartUsage(node, source);

			for(String usage : usages.values())
				usagesText.append(Component
					.literal("\n " + cmd + " ").withStyle(ChatFormatting.GREEN))
					.append(Component.literal(usage)
						.withStyle(ChatFormatting.GREEN));
		}

		if(usagesText.getString().isEmpty())
			usagesText.append(Component.literal("\n " + cmd)
				.withStyle(ChatFormatting.GREEN));

		return Component.literal("\n Usage:").withStyle(ChatFormatting.GRAY)
			.append(usagesText);
	}
}
