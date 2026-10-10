package net.novaclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public enum Utils
{
	;

	public static Minecraft mc()
	{
		return net.novaclient.NovaClient.MC;
	}

	public static boolean canUpdate()
	{
		Minecraft mc = mc();
		return mc != null && mc.player != null && mc.level != null;
	}

	public static LocalPlayer player()
	{
		return mc().player;
	}
}
