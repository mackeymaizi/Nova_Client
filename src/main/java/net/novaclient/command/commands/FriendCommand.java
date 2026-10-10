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
import net.novaclient.util.ChatUtils;
import net.novaclient.util.EntityUtils;
import net.novaclient.util.FriendManager;

/** {@code .friend add|remove|list [name]} - manages your friend list. */
public final class FriendCommand extends Command
{
	public FriendCommand()
	{
		super("friend", "Manages your friends.", "friends");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(literal("add")
			.then(argument("name", StringArgumentType.word())
				.suggests((ctx, b) -> net.minecraft.commands.SharedSuggestionProvider
					.suggest(EntityUtils.getPlayers().stream()
						.map(p -> p.getScoreboardName()), b))
				.executes(ctx -> {
					String name = ctx.getArgument("name", String.class);

					if(FriendManager.isFriend(name))
					{
						warning("Already friends with %s.", name);
						return SINGLE_SUCCESS;
					}

					FriendManager.add(name);
					info("Added %s to friends.", name);
					return SINGLE_SUCCESS;
				})));

		builder.then(literal("remove")
			.then(argument("name", StringArgumentType.word())
				.suggests((ctx, b) -> net.minecraft.commands.SharedSuggestionProvider
					.suggest(FriendManager.getFriends(), b))
				.executes(ctx -> {
					String name = ctx.getArgument("name", String.class);

					if(FriendManager.remove(name))
						info("Removed %s from friends.", name);
					else
						warning("Not friends with %s.", name);

					return SINGLE_SUCCESS;
				})));

		builder.then(literal("list").executes(ctx -> {
			var friends = FriendManager.getFriends();

			if(friends.isEmpty())
			{
				warning("You have no friends added.");
				return SINGLE_SUCCESS;
			}

			info("Friends (%d):", friends.size());
			for(String friend : friends)
				ChatUtils.send(" " + friend);

			return SINGLE_SUCCESS;
		}));

		// Quick add the player you are looking at.
		builder.executes(ctx -> {
			if(mc().crosshairPickEntity instanceof Player player)
			{
				String name = player.getScoreboardName();
				FriendManager.add(name);
				info("Added %s to friends.", name);
			}else
				warning("Look at a player or use .friend add <name>.");

			return SINGLE_SUCCESS;
		});
	}
}
