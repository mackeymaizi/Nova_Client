/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.player;

import net.minecraft.world.phys.Vec3;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.DoubleSetting;

/** Ported from Wurst's AntiKnockbackHack. */
public final class AntiKnockbackModule extends Module
{
	private final DoubleSetting hStrength = new DoubleSetting(
		"Horizontal strength", "How far to reduce horizontal knockback.\n"
			+ "-100% = double knockback\n" + "0% = normal knockback\n"
			+ "100% = no knockback\n" + ">100% = reverse knockback",
		1, -1, 2, 0.01);

	private final DoubleSetting vStrength = new DoubleSetting(
		"Vertical strength", "How far to reduce vertical knockback.\n"
			+ "-100% = double knockback\n" + "0% = normal knockback\n"
			+ "100% = no knockback\n" + ">100% = reverse knockback",
		1, -1, 2, 0.01);

	public AntiKnockbackModule()
	{
		super("AntiKnockback", "Reduces or reverses the knockback you take.",
			ModuleCategory.PLAYER);
		displayName("防击退");

		addSetting(hStrength);
		addSetting(vStrength);
	}

	public Vec3 modifyKnockback(Vec3 original)
	{
		if(!isEnabled())
			return original;

		double verticalMultiplier = 1 - vStrength.get();
		double horizontalMultiplier = 1 - hStrength.get();

		return original.multiply(horizontalMultiplier, verticalMultiplier,
			horizontalMultiplier);
	}
}
