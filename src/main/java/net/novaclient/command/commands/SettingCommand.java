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
import net.novaclient.command.arguments.SettingArgumentType;
import net.novaclient.command.arguments.SettingValueArgumentType;
import net.novaclient.module.Module;
import net.novaclient.setting.Setting;
import net.novaclient.setting.Settings;
import net.novaclient.util.ChatUtils;

/**
 * {@code .setting <module> [setting] [value]} - views and changes module
 * settings.
 */
public final class SettingCommand extends Command
{
	public SettingCommand()
	{
		super("setting", "View and change module settings.", "s");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(argument("module", ModuleArgumentType.create())
			.executes(ctx -> {
				Module module = ModuleArgumentType.get(ctx);
				Settings settings = module.settings;

				if(settings.size() == 0)
				{
					info("%s has no settings.", module.name());
					return SINGLE_SUCCESS;
				}

				info("%s settings:", module.name());
				for(Setting<?> setting : settings)
					ChatUtils.send(" " + setting.name() + " = "
						+ setting.get());

				return SINGLE_SUCCESS;
			})
			.then(literal("reset").executes(ctx -> {
				Module module = ModuleArgumentType.get(ctx);
				for(Setting<?> setting : module.settings)
					setting.reset();

				info("Reset all settings of %s.", module.name());
				return SINGLE_SUCCESS;
			}))
			.then(argument("setting", SettingArgumentType.create())
				.executes(ctx -> {
					Setting<?> setting = SettingArgumentType.get(ctx);

					if(setting == null)
					{
						error("Setting not found.");
						return SINGLE_SUCCESS;
					}

					info("%s = %s", setting.name(), setting.get());
					return SINGLE_SUCCESS;
				})
				.then(argument("value", SettingValueArgumentType.create())
					.executes(ctx -> {
						Setting<?> setting = SettingArgumentType.get(ctx);

						if(setting == null)
						{
							error("Setting not found.");
							return SINGLE_SUCCESS;
						}

						if(SettingValueArgumentType.set(ctx, setting))
							info("Set %s to %s.", setting.name(),
								setting.get());
						else
							error("Invalid value.");

						return SINGLE_SUCCESS;
					}))));
	}
}
