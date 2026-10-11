/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient;

import java.nio.file.Files;
import java.nio.file.Path;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.novaclient.command.CommandManager;
import net.novaclient.mixininterface.IMinecraftClient;
import net.novaclient.module.ModuleManager;

public final class NovaClient implements ClientModInitializer
{
	public static final String NAME = "Nova Client";
	public static final String VERSION = "1.3.5";
	public static final String MC_VERSION = "1.21.11";
	public static final String MOD_ID = "nova";

	public static Minecraft MC;
	public static IMinecraftClient IMC;

	public static NovaClient INSTANCE;

	private Path novaFolder;
	private Config config;

	@Override
	public void onInitializeClient()
	{
		System.out.println("Starting " + NAME + " " + VERSION + "...");

		MC = Minecraft.getInstance();
		IMC = (IMinecraftClient)MC;

		novaFolder = createFolder();

		ModuleManager.init();
		CommandManager.init();

		config = new Config(novaFolder.resolve("config"));
		config.load();

		// Set last: other code uses INSTANCE != null as "fully initialized".
		INSTANCE = this;

		System.out.println(NAME + " initialized.");
	}

	public Config getConfig()
	{
		return config;
	}

	public Path getFolder()
	{
		return novaFolder;
	}

	private Path createFolder()
	{
		Path folder = MC.gameDirectory.toPath().resolve("nova");
		try
		{
			Files.createDirectories(folder);
		}catch(Exception e)
		{
			e.printStackTrace();
		}

		return folder;
	}
}
