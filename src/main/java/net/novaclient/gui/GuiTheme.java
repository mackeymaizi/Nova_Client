/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.gui;

/** Shared colors and helpers for Nova's GUI. Nova's signature gradient. */
public final class GuiTheme
{
	/** rgb(37, 99, 235) - gradient top. */
	public static final int PRIMARY_TOP = 0xFF2563EB;

	/** rgb(78, 72, 230) - gradient bottom. */
	public static final int PRIMARY_BOTTOM = 0xFF4E48E6;

	public static final int BACKGROUND = 0xFF101014;
	public static final int PANEL = 0xFF16161C;
	public static final int PANEL_HEADER = 0xFF1C1C24;
	public static final int PANEL_BORDER = 0xFF26262F;

	public static final int TEXT = 0xFFEDEDF2;
	public static final int TEXT_DIM = 0xFF9A9AA8;
	public static final int TEXT_DISABLED = 0xFF6A6A78;

	public static final int HOVER = 0x22FFFFFF;
	public static final int DISABLED = 0xFF3A3A46;

	private GuiTheme()
	{}

	/** Returns the horizontal Nova gradient for a given x range. */
	public static int gradientLeft()
	{
		return PRIMARY_TOP;
	}

	public static int gradientRight()
	{
		return PRIMARY_BOTTOM;
	}
}
