/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.novaclient.NovaClient;
import net.novaclient.command.commands.BindCommand;
import net.novaclient.command.commands.BindsCommand;
import net.novaclient.command.commands.CommandsCommand;
import net.novaclient.command.commands.DamageCommand;
import net.novaclient.command.commands.DisconnectCommand;
import net.novaclient.command.commands.DropCommand;
import net.novaclient.command.commands.EnchantCommand;
import net.novaclient.command.commands.EnderChestCommand;
import net.novaclient.command.commands.FovCommand;
import net.novaclient.command.commands.FriendCommand;
import net.novaclient.command.commands.GamemodeCommand;
import net.novaclient.command.commands.GiveCommand;
import net.novaclient.command.commands.HClipCommand;
import net.novaclient.command.commands.HelpCommand;
import net.novaclient.command.commands.InputCommand;
import net.novaclient.command.commands.LocateCommand;
import net.novaclient.command.commands.ModulesCommand;
import net.novaclient.command.commands.PeekCommand;
import net.novaclient.command.commands.ProfilesCommand;
import net.novaclient.command.commands.ReloadCommand;
import net.novaclient.command.commands.ResetCommand;
import net.novaclient.command.commands.RotationCommand;
import net.novaclient.command.commands.SayCommand;
import net.novaclient.command.commands.ServerCommand;
import net.novaclient.command.commands.SettingCommand;
import net.novaclient.command.commands.SpectateCommand;
import net.novaclient.command.commands.ToggleCommand;
import net.novaclient.command.commands.VClipCommand;
import net.novaclient.command.commands.WaypointCommand;
import net.novaclient.util.ChatUtils;

/** Registry + dispatcher for Nova's commands. */
public final class CommandManager
{
	/** The command prefix - same as Meteor's default. */
	public static final String PREFIX = ".";

	public static final List<Command> COMMANDS = new ArrayList<>();
	public static CommandDispatcher<ClientSuggestionProvider> DISPATCHER =
		new CommandDispatcher<>();

	private CommandManager()
	{}

	public static void init()
	{
		add(new HelpCommand());
		add(new CommandsCommand());
		add(new ToggleCommand());
		add(new BindCommand());
		add(new BindsCommand());
		add(new SettingCommand());
		add(new ModulesCommand());
		add(new ResetCommand());
		add(new ReloadCommand());
		add(new ProfilesCommand());

		add(new SayCommand());
		add(new VClipCommand());
		add(new HClipCommand());
		add(new DamageCommand());
		add(new DropCommand());
		add(new GamemodeCommand());
		add(new GiveCommand());
		add(new EnchantCommand());
		add(new InputCommand());
		add(new DisconnectCommand());
		add(new ServerCommand());
		add(new LocateCommand());
		add(new PeekCommand());
		add(new EnderChestCommand());
		add(new RotationCommand());
		add(new SpectateCommand());
		add(new FovCommand());
		add(new FriendCommand());
		add(new WaypointCommand());

		COMMANDS.sort(Comparator.comparing(Command::getName));

		rebuild();
	}

	/** Rebuilds the Brigadier tree from the command list. */
	public static void rebuild()
	{
		DISPATCHER = new CommandDispatcher<>();
		for(Command command : COMMANDS)
			command.registerTo(DISPATCHER);
	}

	public static void add(Command command)
	{
		COMMANDS.removeIf(existing -> existing.getName()
			.equalsIgnoreCase(command.getName()));
		COMMANDS.add(command);
	}

	/** Runs a command string (without the prefix). */
	public static void dispatch(String message)
	{
		try
		{
			DISPATCHER.execute(message,
				NovaClient.MC.getConnection().getSuggestionsProvider());
		}catch(CommandSyntaxException e)
		{
			ChatUtils.error(e.getMessage());
		}catch(Exception e)
		{
			ChatUtils.error("Failed to run command: " + e.getMessage());
		}
	}

	/** True when the chat message should be handled as a command. */
	public static boolean isCommand(String message)
	{
		return message.startsWith(PREFIX);
	}

	/** Handles a chat message. Returns true when it was consumed. */
	public static boolean onChatInput(String message)
	{
		if(!isCommand(message))
			return false;

		String command = message.substring(PREFIX.length()).trim();
		if(!command.isEmpty())
			dispatch(command);

		return true;
	}

	public static Command get(String name)
	{
		for(Command command : COMMANDS)
			if(command.getName().equalsIgnoreCase(name))
				return command;

		return null;
	}
}
