/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.PacketSendEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.IntSetting;

/** Ported from LiquidBounce's ModuleMaceKill. */
public final class MaceKillModule extends Module
{
	private final IntSetting fallHeight = new IntSetting("Fall height",
		"The maximum fall height to spoof.", 22, 1, 170, 1);

	public MaceKillModule()
	{
		super("MaceKill", "Fakes fall height when attacking with a mace.",
			ModuleCategory.COMBAT);
		displayName("重锤秒杀");

		addSetting(fallHeight);

		subscribe(PacketSendEvent.class, this::onSendPacket);
	}

	private void onSendPacket(PacketSendEvent event)
	{
		if(!(event.packet() instanceof ServerboundInteractPacket packet)
			|| !net.novaclient.util.PacketUtils.isAttack(packet))
			return;

		var mc = NovaClient.MC;
		var player = mc.player;

		if(!player.getMainHandItem().is(Items.MACE))
			return;

		int height = determineHeight();
		if(height <= 0)
			return;

		if(height > 10)
		{
			int steps = (int)Math.ceil(Math.abs(height / 10.0));
			for(int i = 0; i < steps; i++)
				sendPosition(player.position(), false);
		}else
		{
			sendPosition(player.position(), player.onGround());
			sendPosition(player.position(), player.onGround());
		}

		sendPosition(player.position().add(0, height, 0), false);
		sendPosition(player.position(), false);
	}

	/**
	 * Determines the highest fall height at which the player would not
	 * collide with a block. Minecraft has no collision check for
	 * teleports, so in-between heights do not need checking.
	 */
	private int determineHeight()
	{
		var mc = NovaClient.MC;
		var player = mc.player;

		AABB box = player.getBoundingBox();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for(int i = fallHeight.get(); i >= 1; i--)
		{
			AABB moved = box.move(0, i, 0);

			if(mc.level.noCollision(player, moved))
				return i;
		}

		return 0;
	}

	private void sendPosition(Vec3 pos, boolean onGround)
	{
		NovaClient.MC.player.connection
			.send(new ServerboundMovePlayerPacket.Pos(pos.x, pos.y, pos.z,
				onGround, false));
	}
}
