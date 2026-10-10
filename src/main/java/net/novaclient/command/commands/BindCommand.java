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

/**
 * {@code .bind <module> [key]} - binds a module to a key.
 * Without a key argument it binds to the next pressed key.
 */
public final class BindCommand extends Command
{
	/** Module awaiting its keybind, set by {@code .bind <module>}. */
	public static Module awaitingBind;

	public BindCommand()
	{
		super("bind", "Binds a module to a key.");

		// Listen for the next key press while awaiting a bind.
		net.novaclient.event.EventBus.subscribe(
			net.novaclient.event.events.KeyPressEvent.class, this, event -> {
				if(awaitingBind == null
					|| event.action() != org.lwjgl.glfw.GLFW.GLFW_PRESS)
					return;

				Module module = awaitingBind;
				awaitingBind = null;
				module.setKeybind(event.key());
				info("Bound %s to key %d.", module.name(), event.key());
			});
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(argument("module", ModuleArgumentType.create())
			.executes(ctx -> {
				Module module = ModuleArgumentType.get(ctx);
				awaitingBind = module;
				info("Press a key to bind %s to.", module.name());
				return SINGLE_SUCCESS;
			})
			.then(literal("none").executes(ctx -> {
				Module module = ModuleArgumentType.get(ctx);
				module.setKeybind(-1);
				info("Unbound %s.", module.name());
				return SINGLE_SUCCESS;
			})));

		builder.then(literal("list").executes(ctx -> {
			info("Keybinds:");

			boolean any = false;
			for(Module module : ModuleManager.getAll())
				if(module.hasKeybind())
				{
					any = true;
					net.novaclient.util.ChatUtils.send(" " + module.name()
						+ " -> key " + module.keybind());
				}

			if(!any)
				info("No keybinds set.");
			return SINGLE_SUCCESS;
		}));
	}
}
