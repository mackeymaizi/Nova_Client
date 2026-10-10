/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.novaclient.NovaClient;
import net.novaclient.event.events.BlockBreakingProgressEvent;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.mixininterface.IMultiPlayerGameMode;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.IntSetting;

/** Ported from Wurst's FastBreakHack. Breaks blocks faster. */
public final class FastBreakModule extends Module
{
	private final BoolSetting legitMode = new BoolSetting("Legit mode",
		"Only removes the delay between blocks, without speeding up mining.",
		false);

	private final IntSetting activationChance = new IntSetting(
		"Activation chance", "Percentage chance to fast-break a given block.",
		100, 0, 100, 5);

	private final java.util.Random random = new java.util.Random();
	private BlockPos lastBlockPos;
	private boolean fastBreakBlock;

	public FastBreakModule()
	{
		super("FastBreak", "Breaks blocks much faster.", ModuleCategory.WORLD);
		displayName("快速挖掘");

		addSetting(legitMode);
		addSetting(activationChance);

		subscribe(UpdateEvent.class, e -> onUpdate());
		subscribe(BlockBreakingProgressEvent.class, this::onBreakingProgress);
	}

	@Override
	public void onDisable()
	{
		lastBlockPos = null;
	}

	private void onUpdate()
	{
		if(NovaClient.MC.gameMode != null)
			NovaClient.MC.gameMode.destroyDelay = 0;
	}

	private void onBreakingProgress(BlockBreakingProgressEvent event)
	{
		if(legitMode.isChecked())
			return;

		if(NovaClient.MC.gameMode.destroyProgress >= 1)
			return;

		BlockPos pos = event.blockPos();

		if(!pos.equals(lastBlockPos))
		{
			lastBlockPos = pos;
			fastBreakBlock = random.nextInt(100) < activationChance.get();
		}

		if(!fastBreakBlock)
			return;

		Direction direction = NovaClient.MC.hitResult instanceof
			net.minecraft.world.phys.BlockHitResult hit
				? hit.getDirection() : Direction.UP;

		((IMultiPlayerGameMode)NovaClient.MC.gameMode)
			.nova_sendPlayerActionC2SPacket(
				ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos,
				direction);
	}
}
