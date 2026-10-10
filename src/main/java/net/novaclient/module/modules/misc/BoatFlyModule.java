/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.misc;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;

/** Ported from Wurst's BoatFlyHack. */
public final class BoatFlyModule extends Module
{
	private final BoolSetting changeForwardSpeed = new BoolSetting(
		"Change forward speed",
		"Allows Forward Speed to be changed, disables smooth acceleration.",
		false);

	private final DoubleSetting forwardSpeed = new DoubleSetting(
		"Forward speed", 1, 0.05, 5, 0.05);

	private final DoubleSetting upwardSpeed = new DoubleSetting("Upward speed",
		0.3, 0, 5, 0.05);

	public BoatFlyModule()
	{
		super("BoatFly", "Lets you fly while riding a boat or other vehicle.",
			ModuleCategory.MOVEMENT);
		displayName("船飞行");

		addSetting(changeForwardSpeed);
		addSetting(forwardSpeed);
		addSetting(upwardSpeed);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(!player.isPassenger())
			return;

		Entity vehicle = player.getVehicle();
		Vec3 velocity = vehicle.getDeltaMovement();

		double motionX = velocity.x;
		double motionY = 0;
		double motionZ = velocity.z;

		if(NovaClient.MC.options.keyJump.isDown())
			motionY = upwardSpeed.get();
		else if(NovaClient.MC.options.keySprint.isDown())
			motionY = velocity.y;

		if(NovaClient.MC.options.keyUp.isDown()
			&& changeForwardSpeed.isChecked())
		{
			double speed = forwardSpeed.get();
			float yawRad = vehicle.getYRot() * Mth.DEG_TO_RAD;

			motionX = Mth.sin(-yawRad) * speed;
			motionZ = Mth.cos(yawRad) * speed;
		}

		vehicle.setDeltaMovement(motionX, motionY, motionZ);
	}
}
