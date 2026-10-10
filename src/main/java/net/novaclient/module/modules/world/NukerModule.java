/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.EnumSetting;
import net.novaclient.setting.IntSetting;
import net.novaclient.util.BlockUtils;
import net.novaclient.util.BlockUtils.BreakMode;

/** Ported from Wurst's NukerHack. Breaks every block around you. */
public final class NukerModule extends Module
{
	private final DoubleSetting range = new DoubleSetting("Range",
		"Radius in which to break blocks.", 4.5, 1, 6, 0.1);

	private final IntSetting blocksPerTick = new IntSetting("Blocks per tick",
		"How many blocks to break each tick.", 1, 1, 20, 1);

	private final EnumSetting<BreakMode> breakMode = new EnumSetting<>(
		"Break mode",
		"Normal: vanilla mining. Legit: start+continue, bypasses stop-packet "
			+ "checks. Speed: raw packets, fastest but most likely to be "
			+ "flagged.",
		BreakMode.values(), BreakMode.SPEED);

	private final BoolSetting flatMode = new BoolSetting("Flat mode",
		"Won't break any blocks below your feet.", false);

	private final BoolSetting swing = new BoolSetting("Swing hand",
		"Swings your hand while breaking.", true);

	private final BoolSetting onlyExposed = new BoolSetting("Only exposed",
		"Only breaks blocks that touch air.", true);

	public NukerModule()
	{
		super("Nuker", "Breaks all blocks around you.", ModuleCategory.WORLD);
		displayName("范围挖掘");

		addSetting(range);
		addSetting(blocksPerTick);
		addSetting(breakMode);
		addSetting(flatMode);
		addSetting(swing);
		addSetting(onlyExposed);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	@Override
	public String getInfoString()
	{
		return breakMode.get().displayName();
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		var level = NovaClient.MC.level;

		if(player == null || level == null)
			return;

		BlockPos origin = BlockPos.containing(player.position());
		int radius = (int)Math.ceil(range.get());

		List<BlockPos> candidates = new ArrayList<>();

		for(BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius,
			-radius), origin.offset(radius, radius, radius)))
		{
			BlockPos immutable = pos.immutable();

			if(immutable.distSqr(origin) > range.get() * range.get())
				continue;

			if(flatMode.isChecked() && immutable.getY() < origin.getY())
				continue;

			BlockState state = level.getBlockState(immutable);

			if(state.isAir() || BlockUtils.isUnbreakable(immutable))
				continue;

			if(onlyExposed.isChecked() && !isExposed(level, immutable))
				continue;

			candidates.add(immutable);
		}

		candidates.sort(Comparator.comparingDouble(p -> p.distSqr(origin)));

		int count = 0;
		for(BlockPos pos : candidates)
		{
			if(count++ >= blocksPerTick.get())
				break;

			BlockUtils.breakBlock(pos, breakMode.get());
		}

		if(swing.isChecked() && !candidates.isEmpty())
			player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
	}

	private boolean isExposed(net.minecraft.world.level.Level level,
		BlockPos pos)
	{
		for(net.minecraft.core.Direction direction : net.minecraft.core.Direction.values())
			if(level.getBlockState(pos.relative(direction)).isAir())
				return true;

		return false;
	}
}
