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
import net.novaclient.command.arguments.ModuleArgumentType;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleManager;

/** {@code .toggle <module> [on|off]} - toggles a module. */
public final class ToggleCommand extends Command
{
	public ToggleCommand()
	{
		super("toggle", "Toggles a module.", "t");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder
			.then(literal("all")
				.then(literal("on").executes(ctx -> {
					for(Module module : ModuleManager.getAll())
						module.setEnabled(true);
					info("Enabled all modules.");
					return SINGLE_SUCCESS;
				}))
				.then(literal("off").executes(ctx -> {
					for(Module module : ModuleManager.getAll())
						module.setEnabled(false);
					info("Disabled all modules.");
					return SINGLE_SUCCESS;
				})))
			.then(argument("module", ModuleArgumentType.create())
				.executes(ctx -> {
					Module module = ModuleArgumentType.get(ctx);
					module.toggle();
					module.sendToggledMsg();
					return SINGLE_SUCCESS;
				})
				.then(literal("on").executes(ctx -> {
					Module module = ModuleArgumentType.get(ctx);
					module.setEnabled(true);
					module.sendToggledMsg();
					return SINGLE_SUCCESS;
				}))
				.then(literal("off").executes(ctx -> {
					Module module = ModuleArgumentType.get(ctx);
					module.setEnabled(false);
					module.sendToggledMsg();
					return SINGLE_SUCCESS;
				})));
	}
}
