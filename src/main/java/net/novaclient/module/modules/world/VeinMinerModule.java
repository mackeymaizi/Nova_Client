/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.world;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.novaclient.NovaClient;
import net.novaclient.event.events.BlockBreakingProgressEvent;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.EnumSetting;
import net.novaclient.setting.IntSetting;
import net.novaclient.util.BlockUtils;
import net.novaclient.util.BlockUtils.BreakMode;

/**
 * Ported from Wurst's VeinMinerHack. Breaks a whole ore vein at once.
 */
public final class VeinMinerModule extends Module
{
	private final IntSetting range = new IntSetting("Range",
		"Maximum distance to mine.", 5, 1, 6, 1);

	private final IntSetting maxVeinSize = new IntSetting("Max vein size",
		"Maximum number of blocks to mine in one vein.", 64, 1, 512, 1);

	private final BoolSetting flatMode = new BoolSetting("Flat mode",
		"Won't break any blocks below your feet.", false);

	private final BoolSetting checkLos = new BoolSetting("Check line of sight",
		"Requires a clear line of sight to each block.", false);

	private final BoolSetting swingHand = new BoolSetting("Swing hand",
		"Swing your hand when mining each block.", true);

	private final EnumSetting<BreakMode> breakMode = new EnumSetting<>(
		"Break mode",
		"Legit: start+continue packets, safer. Speed: raw packets, fastest.",
		new BreakMode[]{BreakMode.LEGIT, BreakMode.SPEED}, BreakMode.SPEED);

	private final Queue<BlockPos> pending = new ArrayDeque<>();
	private final Set<BlockPos> mined = new HashSet<>();
	private int timer;

	public VeinMinerModule()
	{
		super("VeinMiner", "Mines an entire ore vein at once.",
			ModuleCategory.WORLD);
		displayName("连锁挖矿");

		addSetting(range);
		addSetting(maxVeinSize);
		addSetting(breakMode);
		addSetting(flatMode);
		addSetting(checkLos);
		addSetting(swingHand);

		subscribe(BlockBreakingProgressEvent.class, this::onBreaking);
		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	@Override
	public void onDisable()
	{
		pending.clear();
		mined.clear();
	}

	private void onBreaking(BlockBreakingProgressEvent event)
	{
		BlockPos origin = event.blockPos();
		BlockState originState = BlockUtils.getState(origin);

		if(originState.isAir() || mined.contains(origin))
			return;

		collectVein(origin, originState.getBlock());
	}

	private void collectVein(BlockPos origin, Block block)
	{
		pending.clear();
		mined.clear();
		pending.add(origin);

		Set<BlockPos> visited = new HashSet<>();
		int limit = maxVeinSize.get();

		while(!pending.isEmpty() && mined.size() < limit)
		{
			BlockPos pos = pending.poll();

			if(!visited.add(pos) || mined.contains(pos))
				continue;

			if(pos.distSqr(origin) > (double)range.get() * range.get())
				continue;

			if(flatMode.isChecked() && pos.getY() < origin.getY())
				continue;

			BlockState state = BlockUtils.getState(pos);

			if(state.isAir() || state.getBlock() != block)
				continue;

			if(checkLos.isChecked() && !canSee(pos))
				continue;

			mined.add(pos);

			for(net.minecraft.core.Direction direction : net.minecraft.core.Direction.values())
				pending.add(pos.relative(direction));
		}
	}

	private boolean canSee(BlockPos pos)
	{
		var player = NovaClient.MC.player;
		if(player == null)
			return true;

		net.minecraft.world.phys.Vec3 center =
			net.minecraft.world.phys.Vec3.atCenterOf(pos);

		var hit = BlockUtils.raycast(player.getEyePosition(), center,
			net.minecraft.world.level.ClipContext.Fluid.NONE);

		return hit == null || hit.getBlockPos().equals(pos);
	}

	private void onUpdate()
	{
		if(mined.isEmpty())
			return;

		if(timer > 0)
		{
			timer--;
			return;
		}

		timer = 0;

		for(BlockPos pos : new HashSet<>(mined))
		{
			if(BlockUtils.getState(pos).isAir())
			{
				mined.remove(pos);
				continue;
			}

			BlockUtils.breakBlock(pos, breakMode.get());

			if(swingHand.isChecked() && NovaClient.MC.player != null)
				NovaClient.MC.player.swing(
					net.minecraft.world.InteractionHand.MAIN_HAND);

			break;
		}
	}

	@Override
	public String getInfoString()
	{
		return String.valueOf(mined.size());
	}
}
