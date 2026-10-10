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
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.WaypointsModule;

/** {@code .waypoint add|remove|list [name]} - manages waypoints. */
public final class WaypointCommand extends Command
{
	public WaypointCommand()
	{
		super("waypoint", "Manages waypoints.", "wp");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(literal("add")
			.then(argument("name", StringArgumentType.word()).executes(ctx -> {
				String name = ctx.getArgument("name", String.class);
				WaypointsModule waypoints = waypoints();

				if(waypoints.add(name, mc().player.position()))
					info("Added waypoint \"%s\".", name);
				else
					warning("A waypoint called \"%s\" already exists.", name);

				return SINGLE_SUCCESS;
			})));

		builder.then(literal("remove")
			.then(argument("name", StringArgumentType.word())
				.suggests((ctx, b) -> net.minecraft.commands.SharedSuggestionProvider
					.suggest(waypoints().waypoints().stream()
						.map(WaypointsModule.Waypoint::name), b))
				.executes(ctx -> {
					String name = ctx.getArgument("name", String.class);

					if(waypoints().remove(name))
						info("Removed waypoint \"%s\".", name);
					else
						warning("No waypoint called \"%s\".", name);

					return SINGLE_SUCCESS;
				})));

		builder.then(literal("list").executes(ctx -> {
			var list = waypoints().waypoints();

			if(list.isEmpty())
			{
				warning("No waypoints saved.");
				return SINGLE_SUCCESS;
			}

			info("Waypoints (%d):", list.size());
			for(WaypointsModule.Waypoint waypoint : list)
				net.novaclient.util.ChatUtils.send(String.format(" %s (%.0f, %.0f, %.0f)",
					waypoint.name(), waypoint.pos().x, waypoint.pos().y,
					waypoint.pos().z));

			return SINGLE_SUCCESS;
		}));

		builder.then(literal("clear").executes(ctx -> {
			waypoints().waypoints().clear();
			info("Cleared all waypoints.");
			return SINGLE_SUCCESS;
		}));
	}

	private WaypointsModule waypoints()
	{
		return ModuleManager.get(WaypointsModule.class);
	}
}
