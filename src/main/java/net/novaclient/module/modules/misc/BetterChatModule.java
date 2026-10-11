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
import net.novaclient.setting.StringSetting;

/** Ported from Meteor's BetterChat. Small quality-of-life chat tweaks. */
public final class BetterChatModule extends Module
{
	private static final java.util.regex.Pattern COORDS = java.util.regex.Pattern
		.compile("(-?\\d+\\.\\d+|[~^]-?\\d*\\.?\\d*), ?"
			+ "(-?\\d+\\.\\d+|[~^]-?\\d*\\.?\\d*), ?"
			+ "(-?\\d+\\.\\d+|[~^]-?\\d*\\.?\\d*)");

	private final BoolSetting antiSpam = new BoolSetting("Anti spam",
		"Suppresses identical messages that repeat within a short window.",
		true);

	private final BoolSetting infiniteLength = new BoolSetting(
		"Infinite length", "Removes the character limit from the chat box.",
		true);

	private final BoolSetting keepHistory = new BoolSetting("Keep history",
		"Keeps your chat history when you die or change worlds.", true);

	private final BoolSetting timestamps = new BoolSetting("Timestamps",
		"Prefixes every chat message with the current time.", false);

	private final BoolSetting showSeconds = new BoolSetting("Show seconds",
		"Include seconds in the timestamp.", false);

	private final BoolSetting coordsProtection = new BoolSetting(
		"Coords protection",
		"Hides coordinates in incoming chat so you cannot leak them.", false);

	private final BoolSetting regexFilter = new BoolSetting("Regex filter",
		"Hides chat lines that match the regex below.", false);

	private final StringSetting regexFilterValue = new StringSetting(
		"Filter regex", "The regex used by the regex filter.", "");

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
		addSetting(timestamps);
		addSetting(showSeconds);
		addSetting(coordsProtection);
		addSetting(regexFilter);
		addSetting(regexFilterValue);
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

	public boolean isCoordsProtection()
	{
		return isEnabled() && coordsProtection.isChecked();
	}

	/** Timestamp prefix for incoming chat, or null when disabled. */
	public String timestampPrefix()
	{
		if(!isEnabled() || !timestamps.isChecked())
			return null;

		return "\u00a78[" + new java.text.SimpleDateFormat(
			showSeconds.isChecked() ? "HH:mm:ss" : "HH:mm")
				.format(new java.util.Date()) + "] \u00a7r";
	}

	/** Hides coordinates in an incoming chat line. */
	public String protectCoords(String message)
	{
		if(!isCoordsProtection())
			return message;

		return COORDS.matcher(message).replaceAll("(x, y, z)");
	}

	/** Rewrites an incoming chat line, or returns null to hide it entirely. */
	public String rewrite(String message)
	{
		if(isEnabled() && regexFilter.isChecked()
			&& !regexFilterValue.get().isEmpty())
		{
			try
			{
				if(java.util.regex.Pattern.compile(regexFilterValue.get())
					.matcher(message).find())
					return null;
			}catch(java.util.regex.PatternSyntaxException ignored)
			{
			}
		}

		String prefix = timestampPrefix();
		String body = protectCoords(message);

		return prefix == null ? body : prefix + body;
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
