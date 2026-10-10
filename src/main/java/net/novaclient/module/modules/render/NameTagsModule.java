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
import net.novaclient.setting.DoubleSetting;

/** Ported from Wurst's NameTagsHack. */
public final class NameTagsModule extends Module
{
	private final DoubleSetting scale = new DoubleSetting("Scale",
		"How large the nametags should be.", 1, 0.05, 5, 0.05);

	private final BoolSetting unlimitedRange = new BoolSetting("Unlimited range",
		"Removes the 64 block distance limit for nametags.", true);

	private final BoolSetting seeThrough = new BoolSetting("See-through mode",
		"Renders nametags on the see-through text layer. This makes them"
			+ " easier to read behind walls, but causes some graphical glitches"
			+ " with water and other transparent things.",
		false);

	private final BoolSetting forceMobNametags = new BoolSetting(
		"Always show named mobs", "Displays the nametags of named mobs even"
			+ " when you are not looking directly at them.",
		true);

	private final BoolSetting forcePlayerNametags = new BoolSetting(
		"Always show player names", "Displays your own nametag as well as any"
			+ " player names that would normally be disabled by scoreboard team"
			+ " settings.",
		false);

	public NameTagsModule()
	{
		super("NameTags", "Changes the way nametags are rendered.",
			ModuleCategory.RENDER);
		displayName("名牌");

		addSetting(scale);
		addSetting(unlimitedRange);
		addSetting(seeThrough);
		addSetting(forceMobNametags);
		addSetting(forcePlayerNametags);
	}

	public float getScale()
	{
		return scale.get().floatValue();
	}

	public boolean isUnlimitedRange()
	{
		return isEnabled() && unlimitedRange.isChecked();
	}

	public boolean isSeeThrough()
	{
		return isEnabled() && seeThrough.isChecked();
	}

	public boolean shouldForceMobNametags()
	{
		return isEnabled() && forceMobNametags.isChecked();
	}

	public boolean shouldForcePlayerNametags()
	{
		return isEnabled() && forcePlayerNametags.isChecked();
	}
}
