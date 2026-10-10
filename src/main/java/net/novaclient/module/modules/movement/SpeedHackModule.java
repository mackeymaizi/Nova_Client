/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.DoubleSetting;

/** Ported from Wurst's SpeedHackHack. */
public final class SpeedHackModule extends Module
{
	private final DoubleSetting speed = new DoubleSetting("Speed",
		"How much to speed up your movement.", 1.8, 1, 10, 0.05);

	private final DoubleSetting maxSpeed = new DoubleSetting("Max speed",
		"The maximum horizontal speed. Lower values are less likely to be"
			+ " detected by anti-cheats.",
		0.66, 0.1, 10, 0.01);

	public SpeedHackModule()
	{
		super("SpeedHack", "Makes you walk faster.", ModuleCategory.MOVEMENT);
		displayName("加速");

		addSetting(speed);
		addSetting(maxSpeed);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;

		if(player.isShiftKeyDown())
			return;

		Vec2 move = player.input.getMoveVector();
		if(move.x == 0 && move.y == 0)
			return;

		if(move.y > 0 && !player.horizontalCollision)
			player.setSprinting(true);

		if(!player.onGround())
			return;

		Vec3 v = player.getDeltaMovement();
		player.setDeltaMovement(v.x * speed.get(), v.y + 0.1,
			v.z * speed.get());

		v = player.getDeltaMovement();
		double currentSpeed = Math.sqrt(v.x * v.x + v.z * v.z);

		double max = maxSpeed.get();
		if(currentSpeed > max)
			player.setDeltaMovement(v.x / currentSpeed * max, v.y,
				v.z / currentSpeed * max);
	}
}
