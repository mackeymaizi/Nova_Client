/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.arguments;

import java.util.ArrayList;
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
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.EnumSetting;
import net.novaclient.setting.Setting;

/** Parses a setting value and applies it to the resolved setting. */
public final class SettingValueArgumentType implements ArgumentType<String>
{
	private static final SettingValueArgumentType INSTANCE =
		new SettingValueArgumentType();

	private SettingValueArgumentType()
	{}

	public static SettingValueArgumentType create()
	{
		return INSTANCE;
	}

	public static String get(CommandContext<?> context)
	{
		return context.getArgument("value", String.class);
	}

	/** Applies the parsed value to the setting. Returns true on success. */
	public static boolean set(CommandContext<?> context, Setting<?> setting)	{
		String value = get(context);

		if(setting instanceof BoolSetting bool)
		{
			bool.setChecked(Boolean.parseBoolean(value));
			return true;
		}

		if(setting instanceof EnumSetting<?> enumSetting)
			return setEnum(enumSetting, value);

		if(setting instanceof net.novaclient.setting.IntSetting intSetting)
		{
			try
			{
				intSetting.set(Integer.parseInt(value));
				return true;
			}catch(NumberFormatException e)
			{
				return false;
			}
		}

		if(setting instanceof net.novaclient.setting.DoubleSetting doubleSetting)
		{
			try
			{
				doubleSetting.set(Double.parseDouble(value));
				return true;
			}catch(NumberFormatException e)
			{
				return false;
			}
		}

		if(setting instanceof net.novaclient.setting.StringSetting stringSetting)
		{
			stringSetting.set(value);
			return true;
		}

		if(setting instanceof net.novaclient.setting.ColorSetting colorSetting)
		{
			try
			{
				colorSetting.set(new java.awt.Color(
					(int)Long.parseLong(value.replace("#", ""), 16), true));
				return true;
			}catch(NumberFormatException e)
			{
				return false;
			}
		}

		return false;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static boolean setEnum(EnumSetting enumSetting, String value)
	{
		for(Object constant : enumSetting.values())
			if(((Enum<?>)constant).name().equalsIgnoreCase(value))
			{
				enumSetting.set((Enum)constant);
				return true;
			}

		return false;
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
		Setting<?> setting;
		try
		{
			setting = SettingArgumentType.get(context);
		}catch(Exception e)
		{
			return builder.buildFuture();
		}

		if(setting == null)
			return Suggestions.empty();

		return SharedSuggestionProvider.suggest(suggestions(setting), builder);
	}

	private static List<String> suggestions(Setting<?> setting)
	{
		List<String> list = new ArrayList<>();

		if(setting instanceof BoolSetting)
		{
			list.add("true");
			list.add("false");
		}else if(setting instanceof EnumSetting<?> enumSetting)
		{
			for(Object constant : enumSetting.values())
				list.add(((Enum<?>)constant).name());
		}

		return list;
	}

	@Override
	public Collection<String> getExamples()
	{
		return List.of("true", "false");
	}
}
