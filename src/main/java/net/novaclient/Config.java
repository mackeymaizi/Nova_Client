/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.novaclient.module.Module;
import net.novaclient.module.ModuleManager;

public final class Config
{
	private static final Gson GSON =
		new GsonBuilder().setPrettyPrinting().create();

	private final Path folder;
	private final Path modulesFile;

	public Config(Path folder)
	{
		this.folder = folder;
		this.modulesFile = folder.resolve("modules.json");
	}

	public void load()
	{
		if(!Files.isRegularFile(modulesFile))
			return;

		try
		{
			String json = Files.readString(modulesFile);
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();

			for(Module module : ModuleManager.getAll())
			{
				JsonElement element = root.get(module.name());
				if(element == null || !element.isJsonObject())
					continue;

				JsonObject object = element.getAsJsonObject();
				readEnabled(module, object);
				readKeybind(module, object);
				module.settings.fromJson(object);
			}

		}catch(Exception e)
		{
			System.err.println("[Nova] Failed to load config: " + e);
		}
	}

	public void save()
	{
		try
		{
			Files.createDirectories(folder);

			JsonObject root = new JsonObject();
			for(Module module : ModuleManager.getAll())
			{
				JsonObject object = module.settings.toJson();
				object.addProperty("enabled", module.isEnabled());
				object.addProperty("keybind", module.keybind());
				root.add(module.name(), object);
			}

			Files.writeString(modulesFile, GSON.toJson(root));

		}catch(IOException e)
		{
			System.err.println("[Nova] Failed to save config: " + e);
		}
	}

	/** Loadable/savable list of profiles, used by the ClickGUI. */
	public List<String> listProfiles()
	{
		List<String> profiles = new ArrayList<>();
		Path profilesFolder = folder.resolve("profiles");
		if(!Files.isDirectory(profilesFolder))
			return profiles;

		try(Stream<Path> stream = Files.list(profilesFolder))
		{
			stream.filter(Files::isRegularFile).forEach(p -> {
				String name = p.getFileName().toString();
				if(name.endsWith(".json"))
					profiles.add(name.substring(0, name.length() - 5));
			});
		}
		catch(IOException e)
		{
			System.err.println("[Nova] Failed to list profiles: " + e);
		}

		profiles.sort(String::compareToIgnoreCase);
		return profiles;
	}

	private void readEnabled(Module module, JsonObject object)
	{
		JsonElement element = object.get("enabled");
		if(element == null || !element.isJsonPrimitive())
			return;

		module.setEnabled(element.getAsBoolean());
	}

	private void readKeybind(Module module, JsonObject object)
	{
		JsonElement element = object.get("keybind");
		if(element == null || !element.isJsonPrimitive())
			return;

		module.setKeybind(element.getAsInt());
	}
}
