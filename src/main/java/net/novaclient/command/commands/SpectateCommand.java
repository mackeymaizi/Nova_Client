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

/** {@code .spectate [player]} - spectates a player from the camera. */
public final class SpectateCommand extends Command
{
	public SpectateCommand()
	{
		super("spectate", "Allows you to spectate nearby players.", "spec");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.executes(ctx -> {
			mc().setCameraEntity(mc().player);
			info("Stopped spectating.");
			return SINGLE_SUCCESS;
		});

		builder.then(literal("reset").executes(ctx -> {
			mc().setCameraEntity(mc().player);
			return SINGLE_SUCCESS;
		}));

		builder.then(argument("player", StringArgumentType.word())
			.suggests((ctx, b) -> net.minecraft.commands.SharedSuggestionProvider
				.suggest(EntityUtils.getPlayers().stream()
					.map(p -> p.getScoreboardName()), b))
			.executes(ctx -> {
				String name = ctx.getArgument("player", String.class);

				for(Player player : EntityUtils.getPlayers())
					if(player.getScoreboardName()
						.equalsIgnoreCase(name))
					{
						mc().setCameraEntity(player);
						info("Spectating %s. Press sneak to stop.", name);
						return SINGLE_SUCCESS;
					}

				error("Player \"%s\" not found.", name);
				return SINGLE_SUCCESS;
			}));
	}
}
