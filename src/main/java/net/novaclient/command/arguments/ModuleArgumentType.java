/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.arguments;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleManager;

public final class ModuleArgumentType implements ArgumentType<Module>
{
	private static final ModuleArgumentType INSTANCE = new ModuleArgumentType();

	private static final DynamicCommandExceptionType NO_SUCH_MODULE =
		new DynamicCommandExceptionType(name -> Component
			.literal("Module with name " + name + " doesn't exist."));

	private ModuleArgumentType()
	{}

	public static ModuleArgumentType create()
	{
		return INSTANCE;
	}

	public static Module get(CommandContext<?> context)
	{
		return context.getArgument("module", Module.class);
	}

	@Override
	public Module parse(StringReader reader) throws CommandSyntaxException
	{
		String argument = reader.readString();
		Module module = ModuleManager.get(argument);

		if(module == null)
			throw NO_SUCH_MODULE.create(argument);

		return module;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(
		CommandContext<S> context, SuggestionsBuilder builder)
	{
		return SharedSuggestionProvider.suggest(
			ModuleManager.getAll().stream().map(Module::name), builder);
	}

	@Override
	public Collection<String> getExamples()
	{
		return ModuleManager.getAll().stream().limit(3).map(Module::name)
			.toList();
	}
}
