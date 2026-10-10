/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.novaclient.command.Command;

/** {@code .fov <fov>} - changes your field of view. */
public final class FovCommand extends Command
{
	public FovCommand()
	{
		super("fov", "Changes your FOV.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(argument("fov", IntegerArgumentType.integer(1, 180))
			.executes(ctx -> {
				int fov = IntegerArgumentType.getInteger(ctx, "fov");
				mc().options.fov().set(fov);
				info("FOV set to %d.", fov);
				return SINGLE_SUCCESS;
			}));
	}
}
