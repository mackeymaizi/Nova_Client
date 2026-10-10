/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;

/** Ported from Wurst's FlightHack. */
public final class FlightModule extends Module
{
	private final DoubleSetting horizontalSpeed = new DoubleSetting(
		"Horizontal speed", "Horizontal flight speed.", 1, 0.05, 10, 0.05);

	private final DoubleSetting verticalSpeed = new DoubleSetting(
		"Vertical speed", "Vertical flight speed.", 1, 0.05, 5, 0.05);

	private final BoolSetting antiKick = new BoolSetting("Anti-Kick",
		"Slightly moves you down every so often to avoid being kicked.",
		false);

	private int tickCounter;

	public FlightModule()
	{
		super("Flight", "Allows you to fly.", ModuleCategory.MOVEMENT);
		displayName("飞行");

		addSetting(horizontalSpeed);
		addSetting(verticalSpeed);
		addSetting(antiKick);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	@Override
	public void onEnable()
	{
		tickCounter = 0;
	}

	@Override
	public String getInfoString()
	{
		return String.format("%.2f", horizontalSpeed.get());
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player == null)
			return;

		player.setDeltaMovement(Vec3.ZERO);
		player.getAbilities().flying = false;

		double vSpeed = verticalSpeed.get();

		if(NovaClient.MC.options.keyJump.isDown())
			player.addDeltaMovement(new Vec3(0, vSpeed, 0));

		if(NovaClient.MC.options.keyShift.isDown())
			player.addDeltaMovement(new Vec3(0, -vSpeed, 0));

		// Horizontal movement from the WASD input vector.
		Vec2 move = player.input.getMoveVector();
		if(move.lengthSquared() > 1.0E-5F)
		{
			float yawRad = player.getYRot() * Mth.DEG_TO_RAD;
			Vec3 forward = new Vec3(-Mth.sin(yawRad), 0, Mth.cos(yawRad));
			Vec3 right = new Vec3(forward.z, 0, -forward.x);

			Vec3 motion = forward.scale(move.y).add(right.scale(move.x))
				.normalize().scale(horizontalSpeed.get());

			player.addDeltaMovement(motion);
		}

		if(antiKick.isChecked())
			doAntiKick(player);
	}

	private void doAntiKick(net.minecraft.client.player.LocalPlayer player)
	{
		tickCounter++;

		if(tickCounter < 70)
			return;

		player.addDeltaMovement(new Vec3(0, -0.035, 0));
		tickCounter = 0;
	}
}
