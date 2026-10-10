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
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.IntSetting;
import net.novaclient.util.BlockUtils;
import net.novaclient.util.RotationUtils;

/**
 * Ported from Wurst's KaboomHack. Plays an explosion and then breaks every
 * block around you in a single tick, spamming the server with destroy
 * packets. Turns itself off right after firing.
 */
public final class KaboomModule extends Module
{
	private final IntSetting power = new IntSetting("Power",
		"How many times to spam the destroy packets. Higher = more reliable, "
			+ "but more likely to get you kicked.",
		128, 32, 512, 32);

	private final IntSetting range = new IntSetting("Range",
		"Radius of the blast, in blocks.", 6, 1, 8, 1);

	private final BoolSetting sound = new BoolSetting("Sound",
		"Plays the explosion sound.", true);

	private final BoolSetting particles = new BoolSetting("Particles",
		"Spawns the explosion particle.", true);

	private final Random random = new Random();

	public KaboomModule()
	{
		super("Kaboom", "Breaks every block around you, with a bang.",
			ModuleCategory.WORLD);
		displayName("轰爆");

		addSetting(power);
		addSetting(range);
		addSetting(sound);
		addSetting(particles);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	@Override
	public String getInfoString()
	{
		return power.get() + "x";
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		var level = NovaClient.MC.level;

		if(player == null || level == null)
			return;

		// Abort while airborne to avoid getting kicked by the server.
		if(!player.getAbilities().instabuild && !player.onGround())
			return;

		double x = player.getX();
		double y = player.getY();
		double z = player.getZ();

		if(sound.isChecked())
		{
			float soundPitch =
				(1F + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.7F;
			level.playLocalSound(x, y, z, SoundEvents.GENERIC_EXPLODE.value(),
				SoundSource.BLOCKS, 4, soundPitch, false);
		}

		if(particles.isChecked())
			level.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0,
				0);

		ArrayList<BlockPos> blocks = getBlocksByDistanceReversed();
		for(int i = 0; i < power.get(); i++)
			BlockUtils.breakBlocksWithPacketSpam(blocks);

		setEnabled(false);
	}

	/** All non-air blocks in the blast radius, farthest ones first. */
	private ArrayList<BlockPos> getBlocksByDistanceReversed()
	{
		var level = NovaClient.MC.level;
		Vec3 eyesVec = RotationUtils.getEyesPos();
		BlockPos eyesBlock = BlockPos.containing(eyesVec);

		int blockRange = range.get();
		double rangeSq = (double)blockRange * blockRange;

		ArrayList<BlockPos> result = new ArrayList<>();

		for(BlockPos pos : BlockPos.betweenClosed(
			eyesBlock.offset(-blockRange, -blockRange, -blockRange),
			eyesBlock.offset(blockRange, blockRange, blockRange)))
		{
			BlockPos immutable = pos.immutable();

			if(immutable.distToCenterSqr(eyesVec) > rangeSq)
				continue;

			if(level.getBlockState(immutable).isAir()
				|| BlockUtils.isUnbreakable(immutable))
				continue;

			result.add(immutable);
		}

		result.sort(Comparator
			.comparingDouble((BlockPos p) -> -p.distToCenterSqr(eyesVec)));
		return result;
	}
}
