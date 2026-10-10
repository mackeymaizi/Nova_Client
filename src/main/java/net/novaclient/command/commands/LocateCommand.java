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
import net.minecraft.world.entity.player.Player;
import net.novaclient.command.Command;
import net.novaclient.util.EntityUtils;

/**
 * {@code .locate player <name>} - prints a player's coordinates.
 * {@code .locate structure <id>} - forwards to the server's /locate.
 */
public final class LocateCommand extends Command
{
	public LocateCommand()
	{
		super("locate", "Locates players and structures.", "loc");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(literal("player")
			.then(argument("name", StringArgumentType.word())
				.suggests((ctx, b) -> net.minecraft.commands.SharedSuggestionProvider
					.suggest(EntityUtils.getPlayers().stream()
						.map(p -> p.getScoreboardName()), b))
				.executes(ctx -> {
					String name = ctx.getArgument("name", String.class);

					for(Player player : EntityUtils.getPlayers())
						if(player.getScoreboardName()
							.equalsIgnoreCase(name))
						{
							info("%s is at %.1f, %.1f, %.1f", name,
								player.getX(), player.getY(), player.getZ());
							return SINGLE_SUCCESS;
						}

					error("Player \"%s\" not found.", name);
					return SINGLE_SUCCESS;
				})));

		builder.then(literal("structure")
			.then(argument("id", StringArgumentType.word()).executes(ctx -> {
				String id = ctx.getArgument("id", String.class);
				mc().player.connection.sendCommand("locate structure " + id);
				return SINGLE_SUCCESS;
			})));

		builder.then(literal("biome")
			.then(argument("id", StringArgumentType.word()).executes(ctx -> {
				String id = ctx.getArgument("id", String.class);
				mc().player.connection.sendCommand("locate biome " + id);
				return SINGLE_SUCCESS;
			})));
	}
}
