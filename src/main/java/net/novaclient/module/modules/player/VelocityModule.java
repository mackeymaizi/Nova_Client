/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.player;

import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.EnumSetting;

/**
 * Ported from Meteor's Velocity. Fine-grained control over the knockback you
 * take, including explosion and fluid push handling.
 */
public final class VelocityModule extends Module
{
	private final DoubleSetting horizontal = new DoubleSetting("Horizontal",
		"0% = vanilla, 100% = no knockback.", 0, -1, 2, 0.01);

	private final DoubleSetting vertical = new DoubleSetting("Vertical",
		"0% = vanilla, 100% = no knockback.", 0, -1, 2, 0.01);

	private final BoolSetting explosions = new BoolSetting("Explosions",
		"Also cancels knockback from explosions.", true);

	private final BoolSetting fluidPush = new BoolSetting("Fluid push",
		"Also cancels being pushed by water and lava.", false);

	private final BoolSetting entityPush = new BoolSetting("Entity push",
		"Also cancels being pushed by other entities.", false);

	private final EnumSetting<Mode> mode = new EnumSetting<>("Mode",
		"How knockback is applied.", Mode.values(), Mode.Normal);

	public VelocityModule()
	{
		super("Velocity", "Reduces or cancels the knockback you take.",
			ModuleCategory.PLAYER);
		displayName("击退控制");

		addSetting(horizontal);
		addSetting(vertical);
		addSetting(explosions);
		addSetting(fluidPush);
		addSetting(entityPush);
		addSetting(mode);
	}

	public Vec3 modify(Vec3 original)
	{
		if(!isEnabled() || mode.get() == Mode.Normal)
			return original;

		if(mode.get() == Mode.Cancel)
			return Vec3.ZERO;

		double h = 1 - horizontal.get();
		double v = 1 - vertical.get();

		return original.multiply(h, v, h);
	}

	public boolean cancelsExplosions()
	{
		return isEnabled() && explosions.isChecked();
	}

	public boolean cancelsFluidPush()
	{
		return isEnabled() && fluidPush.isChecked();
	}

	public boolean cancelsEntityPush()
	{
		return isEnabled() && entityPush.isChecked();
	}

	public enum Mode
	{
		/** Leave vanilla knockback alone (only the extra toggles apply). */
		Normal,
		/** Scale the knockback by the Horizontal/Vertical sliders. */
		Modified,
		/** Remove knockback entirely. */
		Cancel;
	}
}
