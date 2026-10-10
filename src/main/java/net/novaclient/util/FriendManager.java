/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import net.novaclient.NovaClient;

/** A minimal, persistent friend list. */
public final class FriendManager
{
	private static final Set<String> FRIENDS = new LinkedHashSet<>();
	private static boolean loaded;

	private FriendManager()
	{}

	public static void add(String name)
	{
		load();
		FRIENDS.add(name.toLowerCase());
		save();
	}

	public static boolean remove(String name)
	{
		load();
		boolean removed = FRIENDS.remove(name.toLowerCase());
		if(removed)
			save();

		return removed;
	}

	public static boolean isFriend(String name)
	{
		load();
		return name != null && FRIENDS.contains(name.toLowerCase());
	}

	public static Set<String> getFriends()
	{
		load();
		return FRIENDS;
	}

	private static Path file()
	{
		return NovaClient.INSTANCE.getFolder().resolve("friends.txt");
	}

	private static void load()
	{
		if(loaded)
			return;

		loaded = true;

		try
		{
			Path path = file();
			if(Files.isRegularFile(path))
				FRIENDS.addAll(Files.readAllLines(path).stream()
					.map(String::trim).filter(s -> !s.isEmpty()).toList());
		}catch(IOException e)
		{
			System.err.println("[Nova] Failed to load friends: " + e);
		}
	}

	private static void save()
	{
		try
		{
			Files.write(file(), FRIENDS);
		}catch(IOException e)
		{
			System.err.println("[Nova] Failed to save friends: " + e);
		}
	}
}
