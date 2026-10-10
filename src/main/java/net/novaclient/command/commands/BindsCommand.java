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
import net.novaclient.util.ChatUtils;

/** {@code .binds} - lists all keybinds. */
public final class BindsCommand extends Command
{
	public BindsCommand()
	{
		super("binds", "Lists all active keybinds.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.executes(ctx -> {
			int count = 0;
			for(Module module : ModuleManager.getAll())
				if(module.hasKeybind())
					count++;

			if(count == 0)
			{
				warning("No keybinds set.");
				return SINGLE_SUCCESS;
			}

			info("Keybinds (%d):", count);

			for(Module module : ModuleManager.getAll())
				if(module.hasKeybind())
					ChatUtils.send(" " + module.name() + " -> key "
						+ module.keybind());

			return SINGLE_SUCCESS;
		});
	}
}
