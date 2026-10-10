/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

public enum EspStyle
{
	BOXES("Boxes", true, false),
	LINES("Lines", false, true),
	LINES_AND_BOXES("Lines and boxes", true, true);

	private final String name;
	private final boolean boxes;
	private final boolean lines;

	private EspStyle(String name, boolean boxes, boolean lines)
	{
		this.name = name;
		this.boxes = boxes;
		this.lines = lines;
	}

	public boolean hasBoxes()
	{
		return boxes;
	}

	public boolean hasLines()
	{
		return lines;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
