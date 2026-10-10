/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.EnumSetting;
import net.novaclient.setting.IntSetting;

/** Ported from Wurst's StepHack. */
public final class StepModule extends Module
{
	private final EnumSetting<Mode> mode = new EnumSetting<>("Mode",
		"Simple steps up multiple blocks. Legit bypasses NoCheat+.",
		Mode.values(), Mode.Legit);

	private final IntSetting height = new IntSetting("Height",
		"Only works in Simple mode.", 1, 1, 10, 1);

	public StepModule()
	{
		super("Step", "Allows you to step up full blocks.",
			ModuleCategory.MOVEMENT);
		displayName("自动上台阶");

		addSetting(mode);
		addSetting(height);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	/** Extra step height applied when running in Simple mode. */
	public float adjustStepHeight(float original)
	{
		if(!isEnabled() || mode.get() != Mode.Simple)
			return original;

		return Math.max(original, height.get() + 0.5F);
	}

	private void onUpdate()
	{
		if(mode.get() != Mode.Legit)
			return;

		var player = NovaClient.MC.player;
		if(player == null || !player.horizontalCollision)
			return;

		if(!player.onGround() || player.onClimbable() || player.isInWater()
			|| player.isInLava())
			return;

		if(player.input.getMoveVector().lengthSquared() <= 1.0E-5F)
			return;

		if(player.jumping)
			return;

		AABB box = player.getBoundingBox().move(0, 0.05, 0).inflate(0.05);
		if(!NovaClient.MC.level.noCollision(player, box.move(0, 1, 0)))
			return;

		double stepHeight = java.util.stream.StreamSupport
			.stream(NovaClient.MC.level.getBlockCollisions(player, box)
				.spliterator(), false)
			.mapToDouble(shape -> shape.max(
				net.minecraft.core.Direction.Axis.Y))
			.max().orElse(Double.NEGATIVE_INFINITY) - player.getY();

		if(stepHeight < 0 || stepHeight > 1)
			return;

		var connection = player.connection;

		connection.send(new ServerboundMovePlayerPacket.Pos(player.getX(),
			player.getY() + 0.42 * stepHeight, player.getZ(), player.onGround(),
			player.horizontalCollision));
		connection.send(new ServerboundMovePlayerPacket.Pos(player.getX(),
			player.getY() + 0.753 * stepHeight, player.getZ(),
			player.onGround(), player.horizontalCollision));

		player.setPos(player.getX(), player.getY() + stepHeight, player.getZ());
	}

	public enum Mode
	{
		Simple, Legit;
	}
}
