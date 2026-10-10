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
import net.minecraft.world.level.GameType;
import net.novaclient.command.Command;

/** {@code .gamemode <mode>} - changes your gamemode client-side. */
public final class GamemodeCommand extends Command
{
	public GamemodeCommand()
	{
		super("gamemode", "Changes your gamemode client-side.", "gm");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		for(GameType gameMode : GameType.values())
			builder.then(literal(gameMode.getName()).executes(ctx -> {
				mc().gameMode.setLocalMode(gameMode);
				info("Gamemode set to %s.", gameMode.getName());
				return SINGLE_SUCCESS;
			}));
	}
}
