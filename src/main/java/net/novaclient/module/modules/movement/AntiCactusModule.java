/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.novaclient.NovaClient;
import net.novaclient.event.events.PreMotionEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;

/** Ported from Wurst's AntiCactusHack. */
public final class AntiCactusModule extends Module
{
	private static final VoxelShape VANILLA_HEIGHT_CACTUS_SHAPE =
		Block.column(16, 0, 15);

	private final BoolSetting preventCactusTopDamage = new BoolSetting(
		"Prevent cactus-top damage",
		"Prevents the damage you take when landing on top of a cactus.", true);

	public AntiCactusModule()
	{
		super("AntiCactus", "Prevents cactus damage.",
			ModuleCategory.MOVEMENT);
		displayName("防仙人掌");

		addSetting(preventCactusTopDamage);

		subscribe(PreMotionEvent.class, e -> onPreMotion());
	}

	public VoxelShape getCollisionShape()
	{
		return preventCactusTopDamage.isChecked() ? Shapes.block()
			: VANILLA_HEIGHT_CACTUS_SHAPE;
	}

	private void onPreMotion()
	{
		var player = NovaClient.MC.player;

		if(!preventCactusTopDamage.isChecked()
			|| player.getDeltaMovement().y > 0 || !isAboveCactus())
			return;

		Vec3 pos = player.position();
		boolean hCollision = player.horizontalCollision;
		var connection = player.connection;

		connection.send(new Pos(pos, true, hCollision));
		connection.send(new Pos(pos.add(0, 1e-6, 0), false, hCollision));
		connection.send(new Pos(pos, true, hCollision));
	}

	private boolean isAboveCactus()
	{
		var player = NovaClient.MC.player;
		AABB below = player.getBoundingBox().move(0, -2, 0);

		BlockPos min = BlockPos.containing(below.minX, below.minY, below.minZ);
		BlockPos max =
			BlockPos.containing(below.maxX, player.getY(), below.maxZ);

		for(BlockPos pos : BlockPos.betweenClosed(min, max))
			if(NovaClient.MC.level.getBlockState(pos).is(Blocks.CACTUS)
				&& player.getY() >= pos.getY() + 1)
				return true;

		return false;
	}
}
