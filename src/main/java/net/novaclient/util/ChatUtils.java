package net.novaclient.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.novaclient.NovaClient;

public enum ChatUtils
{
	;

	private static final String PREFIX = "\u00a7b[";
	private static final String SUFFIX = "\u00a7b] \u00a7r";

	public static void send(String message)
	{
		if(NovaClient.MC.gui == null)
			return;

		NovaClient.MC.gui.hud.getChat()
			.addClientSystemMessage(Component.literal(message));
	}

	public static void nova(String message)
	{
		send(PREFIX + ChatFormatting.DARK_AQUA + "Nova" + SUFFIX + message);
	}

	public static void error(String message)
	{
		nova(ChatFormatting.RED + message);
	}

	public static void warning(String message)
	{
		nova(ChatFormatting.YELLOW + message);
	}

	public static void info(String message)
	{
		nova(message);
	}

	public static MutableComponent prefix()
	{
		return Component.literal("").append(Component.literal("[")
			.withStyle(ChatFormatting.AQUA))
			.append(Component.literal("Nova").withStyle(ChatFormatting.BLUE))
			.append(Component.literal("] ").withStyle(ChatFormatting.AQUA));
	}
}
