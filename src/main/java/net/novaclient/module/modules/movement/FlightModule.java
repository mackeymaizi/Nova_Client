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
import net.novaclient.event.events.MouseScrollEvent;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.IntSetting;

/** Ported from Wurst's FlightHack. */
public final class FlightModule extends Module
{
	private final DoubleSetting horizontalSpeed = new DoubleSetting(
		"Horizontal speed", "Horizontal flight speed.", 1, 0.05, 10, 0.05);

	private final DoubleSetting verticalSpeed = new DoubleSetting(
		"Vertical speed", "Vertical flight speed.", 1, 0.05, 5, 0.05);

	private final BoolSetting allowUnsafeVerticalSpeed = new BoolSetting(
		"Allow unsafe vertical speed",
		"Lets the vertical speed go beyond the values the game would normally allow.",
		false);

	private final BoolSetting scrollToChangeSpeed = new BoolSetting(
		"Scroll to change speed",
		"Change the horizontal/vertical speed by scrolling while flying.", true);

	private final BoolSetting renderSpeed = new BoolSetting("Show speed in HackList",
		"Shows the current flight speed next to the module name.", true);

	private final BoolSetting antiKick = new BoolSetting("Anti-Kick",
		"Slightly moves you down every so often to avoid being kicked.",
		false);

	private final IntSetting antiKickInterval = new IntSetting("Anti-Kick Interval",
		"How often the anti-kick dip is applied, in ticks.", 70, 5, 80, 1);

	private final DoubleSetting antiKickDistance = new DoubleSetting(
		"Anti-Kick Distance", "How far the anti-kick dip moves you down.",
		0.035, 0.01, 0.2, 0.001);

	private int tickCounter;

	public FlightModule()
	{
		super("Flight", "Allows you to fly.", ModuleCategory.MOVEMENT);
		displayName("飞行");

		addSetting(horizontalSpeed);
		addSetting(verticalSpeed);
		addSetting(allowUnsafeVerticalSpeed);
		addSetting(scrollToChangeSpeed);
		addSetting(renderSpeed);
		addSetting(antiKick);
		addSetting(antiKickInterval);
		addSetting(antiKickDistance);

		subscribe(UpdateEvent.class, e -> onUpdate());
		subscribe(MouseScrollEvent.class, this::onScroll);
	}

	private void onScroll(MouseScrollEvent event)
	{
		if(!scrollToChangeSpeed.isChecked())
			return;

		double delta = event.vertical() * 0.05;
		if(delta == 0)
			return;

		horizontalSpeed.set(clampSpeed(horizontalSpeed.get() + delta,
			horizontalSpeed.min(), horizontalSpeed.max()));
		verticalSpeed.set(clampSpeed(verticalSpeed.get() + delta,
			verticalSpeed.min(), verticalSpeed.max()));
	}

	private static double clampSpeed(double value, double min, double max)
	{
		return Math.max(min, Math.min(max, Math.round(value * 100.0) / 100.0));
	}

	private double getActualVerticalSpeed()
	{
		boolean limitVerticalSpeed = !allowUnsafeVerticalSpeed.isChecked()
			&& !NovaClient.MC.player.getAbilities().invulnerable;

		return Mth.clamp(horizontalSpeed.get() * verticalSpeed.get(), 0.05,
			limitVerticalSpeed ? 3.95 : 10);
	}

	@Override
	public void onEnable()
	{
		tickCounter = 0;
	}

	@Override
	public String getInfoString()
	{
		if(!renderSpeed.isChecked())
			return null;

		return String.format("%.2f", horizontalSpeed.get());
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player == null)
			return;

		player.setDeltaMovement(Vec3.ZERO);
		player.getAbilities().flying = false;

		double vSpeed = getActualVerticalSpeed();

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

		if(tickCounter < antiKickInterval.get())
			return;

		player.addDeltaMovement(new Vec3(0, -antiKickDistance.get(), 0));
		tickCounter = 0;
	}
}
