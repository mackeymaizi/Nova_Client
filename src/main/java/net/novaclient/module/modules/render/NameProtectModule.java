/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.StringSetting;

/**
 * Ported from Wurst's NameProtectHack. Hides your own name (and other
 * players') from chat and nametags.
 */
public final class NameProtectModule extends Module
{
	private final StringSetting selfName = new StringSetting("Your name",
		"What your own name is replaced with.", "\u00a7oMe\u00a7r");

	private final StringSetting otherName = new StringSetting("Other players",
		"What other players' names are replaced with.", "\u00a7oPlayer\u00a7r");

	private final BoolSetting chat = new BoolSetting("Chat",
		"Also protects names in chat messages.", true);

	private final BoolSetting nametags = new BoolSetting("Nametags",
		"Also protects names on nametags.", true);

	public NameProtectModule()
	{
		super("NameProtect", "Hides player names from chat and nametags.",
			ModuleCategory.RENDER);
		displayName("名字伪装");

		addSetting(selfName);
		addSetting(otherName);
		addSetting(chat);
		addSetting(nametags);
	}

	public boolean inChat()
	{
		return isEnabled() && chat.isChecked();
	}

	public boolean inNametags()
	{
		return isEnabled() && nametags.isChecked();
	}

	/** Replaces player names inside the given string. */
	public String protect(String input)
	{
		if(!isEnabled() || input == null || input.isEmpty())
			return input;

		String result = input;
		var mc = net.novaclient.NovaClient.MC;

		if(mc.getUser() != null)
		{
			String me = mc.getUser().getName();
			if(me != null && !me.isEmpty() && result.contains(me))
				result = result.replace(me, selfName.get());
		}

		for(var player : net.novaclient.util.EntityUtils.getPlayers())
		{
			String name = player.getScoreboardName();
			if(name != null && !name.isEmpty() && result.contains(name))
				result = result.replace(name, otherName.get());
		}

		return result;
	}
}
