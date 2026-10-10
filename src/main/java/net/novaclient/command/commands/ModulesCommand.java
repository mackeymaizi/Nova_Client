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
import net.novaclient.module.ModuleCategory;
import net.novaclient.module.ModuleManager;
import net.novaclient.util.ChatUtils;

/** {@code .modules [category]} - lists modules. */
public final class ModulesCommand extends Command
{
	public ModulesCommand()
	{
		super("modules", "Lists all modules.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.executes(ctx -> {
			for(ModuleCategory category : ModuleCategory.values())
			{
				var modules = ModuleManager.getByCategory(category);
				if(modules.isEmpty())
					continue;

				info("%s (%d):", category.displayName(), modules.size());

				StringBuilder line = new StringBuilder(" ");
				for(Module module : modules)
				{
					line.append(module.isEnabled() ? "\u00a7a" : "\u00a77")
						.append(module.name()).append("\u00a7r, ");
				}

				ChatUtils.send(line.substring(0, line.length() - 2));
			}

			return SINGLE_SUCCESS;
		});
	}
}
