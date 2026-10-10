/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.misc;

import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.IntSetting;

/** Ported from Meteor's BetterChat. Small quality-of-life chat tweaks. */
public final class BetterChatModule extends Module
{
	private final BoolSetting antiSpam = new BoolSetting("Anti spam",
		"Suppresses identical messages that repeat within a short window.",
		true);

	private final BoolSetting infiniteLength = new BoolSetting(
		"Infinite length", "Removes the character limit from the chat box.",
		true);

	private final BoolSetting keepHistory = new BoolSetting("Keep history",
		"Keeps your chat history when you die or change worlds.", true);

	private final IntSetting antiSpamWindow = new IntSetting("Spam window",
		"Seconds within which duplicate messages are suppressed.", 10, 1, 60,
		1);

	private String lastMessage = "";
	private long lastMessageTime;

	public BetterChatModule()
	{
		super("BetterChat", "Improves the chat.", ModuleCategory.MISC);
		displayName("聊天增强");

		addSetting(antiSpam);
		addSetting(infiniteLength);
		addSetting(keepHistory);
		addSetting(antiSpamWindow);
	}

	public boolean isInfiniteLength()
	{
		return isEnabled() && infiniteLength.isChecked();
	}

	public boolean isKeepHistory()
	{
		return isEnabled() && keepHistory.isChecked();
	}

	/**
	 * Returns true when the message should be suppressed as spam.
	 */
	public boolean isSpam(String message)
	{
		if(!isEnabled() || !antiSpam.isChecked())
			return false;

		long now = System.currentTimeMillis();

		if(message.equals(lastMessage)
			&& now - lastMessageTime < antiSpamWindow.get() * 1000L)
			return true;

		lastMessage = message;
		lastMessageTime = now;
		return false;
	}
}
