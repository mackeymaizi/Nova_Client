/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.arguments;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.novaclient.module.Module;
import net.novaclient.setting.Setting;

/**
 * Parses a setting name. The owning module is taken from the "module"
 * argument that must already be present in the command context.
 */
public final class SettingArgumentType implements ArgumentType<String>
{
	private static final SettingArgumentType INSTANCE =
		new SettingArgumentType();

	private SettingArgumentType()
	{}

	public static SettingArgumentType create()
	{
		return INSTANCE;
	}

	/** Resolves the setting referenced by the "setting" argument. */
	public static Setting<?> get(CommandContext<?> context)
	{
		String name = context.getArgument("setting", String.class);
		Module module = context.getArgument("module", Module.class);

		for(Setting<?> setting : module.settings)
			if(setting.name().equalsIgnoreCase(name))
				return setting;

		return null;
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException
	{
		return reader.readString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(
		CommandContext<S> context, SuggestionsBuilder builder)
	{
		try
		{
			Module module = context.getArgument("module", Module.class);
			return SharedSuggestionProvider.suggest(
				module.settings.all().stream().map(Setting::name), builder);
		}catch(Exception e)
		{
			return builder.buildFuture();
		}
	}

	@Override
	public Collection<String> getExamples()
	{
		return List.of("range", "delay", "mode");
	}
}
