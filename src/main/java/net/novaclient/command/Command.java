/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.novaclient.NovaClient;
import net.novaclient.util.ChatUtils;

/**
 * Base class for Nova's client-side commands. The command tree is built with
 * Brigadier, which gives us Tab-completion for free - something Wurst lacks.
 */
public abstract class Command
{
	protected static final int SINGLE_SUCCESS =
		com.mojang.brigadier.Command.SINGLE_SUCCESS;

	protected static Minecraft mc()
	{
		return NovaClient.MC;
	}

	private final String name;
	private final String description;
	private final List<String> aliases;

	public Command(String name, String description, String... aliases)
	{
		this.name = name;
		this.description = description;
		this.aliases = List.of(aliases);
	}

	/** Helper that infers the {@link ClientSuggestionProvider} generic. */
	protected static <T> RequiredArgumentBuilder<ClientSuggestionProvider, T> argument(
		String name, ArgumentType<T> type)
	{
		return RequiredArgumentBuilder.argument(name, type);
	}

	protected static LiteralArgumentBuilder<ClientSuggestionProvider> literal(
		String name)
	{
		return LiteralArgumentBuilder.literal(name);
	}

	public final void registerTo(
		CommandDispatcher<ClientSuggestionProvider> dispatcher)
	{
		register(dispatcher, name);
		for(String alias : aliases)
			register(dispatcher, alias);
	}

	public void register(CommandDispatcher<ClientSuggestionProvider> dispatcher,
		String name)
	{
		LiteralArgumentBuilder<ClientSuggestionProvider> builder =
			LiteralArgumentBuilder.literal(name);
		build(builder);
		dispatcher.register(builder);
	}

	public abstract void build(
		LiteralArgumentBuilder<ClientSuggestionProvider> builder);

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public List<String> getAliases()
	{
		return aliases;
	}

	@Override
	public String toString()
	{
		return CommandManager.PREFIX + name;
	}

	public void info(String message, Object... args)
	{
		ChatUtils.nova(args.length == 0 ? message : String.format(message, args));
	}

	public void warning(String message, Object... args)
	{
		ChatUtils.warning(
			args.length == 0 ? message : String.format(message, args));
	}

	public void error(String message, Object... args)
	{
		ChatUtils
			.error(args.length == 0 ? message : String.format(message, args));
	}
}
