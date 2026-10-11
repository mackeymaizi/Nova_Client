/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.gui;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

/**
 * Maps GLFW key codes to stable, layout-independent English names.
 *
 * <p>{@link GLFW#glfwGetKeyName} returns names that depend on the user's
 * keyboard layout (and is localized on some platforms), so the ClickGUI uses
 * this fixed table instead. Unknown keys fall back to {@code KEY_<code>}.
 */
public final class KeyNames
{
	private static final Map<Integer, String> NAMES = new HashMap<>();

	private KeyNames()
	{}

	/** English name for a GLFW key code, or {@code NONE} for {@code -1}. */
	public static String of(int key)
	{
		if(key == -1)
			return "NONE";

		String name = NAMES.get(key);
		return name != null ? name : "KEY_" + key;
	}

	private static void put(int key, String name)
	{
		NAMES.put(key, name);
	}

	static
	{
		// Letters.
		for(int i = 0; i < 26; i++)
			put(GLFW.GLFW_KEY_A + i, String.valueOf((char)('A' + i)));

		// Digits (top row).
		for(int i = 0; i < 10; i++)
			put(GLFW.GLFW_KEY_0 + i, String.valueOf((char)('0' + i)));

		// Function keys.
		for(int i = 0; i < 25; i++)
			put(GLFW.GLFW_KEY_F1 + i, "F" + (i + 1));

		// Number pad.
		for(int i = 0; i < 10; i++)
			put(GLFW.GLFW_KEY_KP_0 + i, "NUM" + i);
		put(GLFW.GLFW_KEY_KP_DECIMAL, "NUM.");
		put(GLFW.GLFW_KEY_KP_DIVIDE, "NUM/");
		put(GLFW.GLFW_KEY_KP_MULTIPLY, "NUM*");
		put(GLFW.GLFW_KEY_KP_SUBTRACT, "NUM-");
		put(GLFW.GLFW_KEY_KP_ADD, "NUM+");
		put(GLFW.GLFW_KEY_KP_ENTER, "NUM_ENTER");
		put(GLFW.GLFW_KEY_KP_EQUAL, "NUM=");

		// Punctuation / symbols.
		put(GLFW.GLFW_KEY_SPACE, "SPACE");
		put(GLFW.GLFW_KEY_APOSTROPHE, "APOSTROPHE");
		put(GLFW.GLFW_KEY_COMMA, "COMMA");
		put(GLFW.GLFW_KEY_MINUS, "MINUS");
		put(GLFW.GLFW_KEY_PERIOD, "PERIOD");
		put(GLFW.GLFW_KEY_SLASH, "SLASH");
		put(GLFW.GLFW_KEY_SEMICOLON, "SEMICOLON");
		put(GLFW.GLFW_KEY_EQUAL, "EQUAL");
		put(GLFW.GLFW_KEY_LEFT_BRACKET, "L_BRACKET");
		put(GLFW.GLFW_KEY_BACKSLASH, "BACKSLASH");
		put(GLFW.GLFW_KEY_RIGHT_BRACKET, "R_BRACKET");
		put(GLFW.GLFW_KEY_GRAVE_ACCENT, "GRAVE");
		put(GLFW.GLFW_KEY_WORLD_1, "WORLD_1");
		put(GLFW.GLFW_KEY_WORLD_2, "WORLD_2");

		// Editing / navigation.
		put(GLFW.GLFW_KEY_ESCAPE, "ESC");
		put(GLFW.GLFW_KEY_ENTER, "ENTER");
		put(GLFW.GLFW_KEY_TAB, "TAB");
		put(GLFW.GLFW_KEY_BACKSPACE, "BACKSPACE");
		put(GLFW.GLFW_KEY_INSERT, "INSERT");
		put(GLFW.GLFW_KEY_DELETE, "DELETE");
		put(GLFW.GLFW_KEY_RIGHT, "RIGHT");
		put(GLFW.GLFW_KEY_LEFT, "LEFT");
		put(GLFW.GLFW_KEY_DOWN, "DOWN");
		put(GLFW.GLFW_KEY_UP, "UP");
		put(GLFW.GLFW_KEY_PAGE_UP, "PAGE_UP");
		put(GLFW.GLFW_KEY_PAGE_DOWN, "PAGE_DOWN");
		put(GLFW.GLFW_KEY_HOME, "HOME");
		put(GLFW.GLFW_KEY_END, "END");
		put(GLFW.GLFW_KEY_CAPS_LOCK, "CAPS_LOCK");
		put(GLFW.GLFW_KEY_SCROLL_LOCK, "SCROLL_LOCK");
		put(GLFW.GLFW_KEY_NUM_LOCK, "NUM_LOCK");
		put(GLFW.GLFW_KEY_PRINT_SCREEN, "PRINT_SCREEN");
		put(GLFW.GLFW_KEY_PAUSE, "PAUSE");
		put(GLFW.GLFW_KEY_MENU, "MENU");

		// Modifiers.
		put(GLFW.GLFW_KEY_LEFT_SHIFT, "LEFT_SHIFT");
		put(GLFW.GLFW_KEY_LEFT_CONTROL, "LEFT_CONTROL");
		put(GLFW.GLFW_KEY_LEFT_ALT, "LEFT_ALT");
		put(GLFW.GLFW_KEY_LEFT_SUPER, "LEFT_SUPER");
		put(GLFW.GLFW_KEY_RIGHT_SHIFT, "RIGHT_SHIFT");
		put(GLFW.GLFW_KEY_RIGHT_CONTROL, "RIGHT_CONTROL");
		put(GLFW.GLFW_KEY_RIGHT_ALT, "RIGHT_ALT");
		put(GLFW.GLFW_KEY_RIGHT_SUPER, "RIGHT_SUPER");
	}
}
