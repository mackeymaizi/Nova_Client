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
import net.novaclient.NovaClient;
import net.novaclient.command.Command;

/** {@code .reload} - reloads the config from disk. */
public final class ReloadCommand extends Command
{
	public ReloadCommand()
	{
		super("reload", "Reloads Nova's config from disk.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.executes(ctx -> {
			NovaClient.INSTANCE.getConfig().load();
			info("Config reloaded.");
			return SINGLE_SUCCESS;
		});

		builder.then(literal("save").executes(ctx -> {
			NovaClient.INSTANCE.getConfig().save();
			info("Config saved.");
			return SINGLE_SUCCESS;
		}));
	}
}
