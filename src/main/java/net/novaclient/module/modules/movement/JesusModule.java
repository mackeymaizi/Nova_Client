/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.PacketSendEvent;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.util.BlockUtils;

/** Ported from Wurst's JesusHack. */
public final class JesusModule extends Module
{
	private final BoolSetting bypass = new BoolSetting("NoCheat+ bypass",
		"Bypasses NoCheat+ but slows down your movement.", false);

	private int tickTimer = 10;
	private int packetTimer = 0;

	public JesusModule()
	{
		super("Jesus", "Allows you to walk on water and lava.",
			ModuleCategory.MOVEMENT);
		displayName("水上行走");

		addSetting(bypass);

		subscribe(UpdateEvent.class, e -> onUpdate());
		subscribe(PacketSendEvent.class, e -> onSendPacket(e));
	}

	public boolean shouldBeSolid()
	{
		var player = NovaClient.MC.player;
		return isEnabled() && player != null && player.fallDistance <= 3
			&& !NovaClient.MC.options.keyShift.isDown()
			&& !player.isInWater() && !player.isInLava();
	}

	private void onUpdate()
	{
		if(NovaClient.MC.options.keyShift.isDown())
			return;

		var player = NovaClient.MC.player;

		if(player.isInWater() || player.isInLava())
		{
			Vec3 velocity = player.getDeltaMovement();
			player.setDeltaMovement(velocity.x, 0.11, velocity.z);
			tickTimer = 0;
			return;
		}

		Vec3 velocity = player.getDeltaMovement();
		if(tickTimer == 0)
			player.setDeltaMovement(velocity.x, 0.30, velocity.z);
		else if(tickTimer == 1)
			player.setDeltaMovement(velocity.x, 0, velocity.z);

		tickTimer++;
	}

	private void onSendPacket(PacketSendEvent event)
	{
		var player = NovaClient.MC.player;
		if(player == null)
			return;

		if(!(event.packet() instanceof ServerboundMovePlayerPacket packet))
			return;

		boolean isPos = packet instanceof Pos;
		boolean isPosRot = packet instanceof PosRot;
		if(!isPos && !isPosRot)
			return;

		if(player.isInWater())
			return;

		if(player.fallDistance > 3F)
			return;

		if(!isOverLiquid())
			return;

		packetTimer++;
		if(packetTimer < 4)
			return;

		event.cancel();

		double x = packet.getX(0);
		double y = packet.getY(0);
		double z = packet.getZ(0);

		if(bypass.isChecked() && player.tickCount % 2 == 0)
			y -= 0.05;
		else
			y += 0.05;

		Packet<?> newPacket;
		if(isPos)
			newPacket = new Pos(x, y, z, true, player.horizontalCollision);
		else
			newPacket = new PosRot(x, y, z, packet.getYRot(0),
				packet.getXRot(0), true, player.horizontalCollision);

		player.connection.getConnection().send(newPacket);
	}

	private boolean isOverLiquid()
	{
		return BlockUtils.isOverLiquid(
			NovaClient.MC.player.getBoundingBox().move(0, -0.5, 0));
	}
}
