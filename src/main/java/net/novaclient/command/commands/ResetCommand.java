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
import net.novaclient.command.Command;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleManager;
import net.novaclient.setting.Setting;

/** {@code .reset [module]} - resets settings to their defaults. */
public final class ResetCommand extends Command
{
	public ResetCommand()
	{
		super("reset", "Resets module settings to defaults.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.executes(ctx -> {
			for(Module module : ModuleManager.getAll())
				for(Setting<?> setting : module.settings)
					setting.reset();

			info("Reset all settings.");
			return SINGLE_SUCCESS;
		});

		builder.then(argument("module",
			net.novaclient.command.arguments.ModuleArgumentType.create())
			.executes(ctx -> {
				Module module = net.novaclient.command.arguments.ModuleArgumentType
					.get(ctx);

				for(Setting<?> setting : module.settings)
					setting.reset();

				info("Reset %s.", module.name());
				return SINGLE_SUCCESS;
			}));
	}
}
