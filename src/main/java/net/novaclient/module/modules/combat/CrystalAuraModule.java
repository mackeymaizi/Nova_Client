/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.combat;

import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.TickEvent;
import net.novaclient.mixininterface.IMultiPlayerGameMode;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.EnumSetting;
import net.novaclient.setting.IntSetting;
import net.novaclient.util.EntityUtils;
import net.novaclient.util.RotationUtils;

/**
 * Ported from Meteor's CrystalAura. Places end crystals near the target
 * and detonates any crystal that would damage them.
 */
public final class CrystalAuraModule extends Module
{
	private final DoubleSetting targetRange = new DoubleSetting("Target range",
		"Range in which to target players.", 10, 0, 16, 0.5);

	private final DoubleSetting minDamage = new DoubleSetting("Min damage",
		"Minimum damage the crystal needs to deal to your target.", 6, 0, 36,
		0.5);

	private final DoubleSetting maxSelfDamage = new DoubleSetting(
		"Max self damage", "Maximum damage crystals can deal to yourself.", 6,
		0, 36, 0.5);

	private final BoolSetting antiSuicide = new BoolSetting("Anti suicide",
		"Will not place and break crystals if they will kill you.", true);

	private final BoolSetting rotate = new BoolSetting("Rotate",
		"Rotates towards the crystals being placed/broken.", true);

	private final EnumSetting<AutoSwitchMode> autoSwitch = new EnumSetting<>(
		"Auto switch",
		"Switches to crystals in your hotbar once a target is found.",
		AutoSwitchMode.values(), AutoSwitchMode.Normal);

	private final BoolSetting doPlace = new BoolSetting("Place",
		"If CrystalAura should place crystals.", true);

	private final IntSetting placeDelay = new IntSetting("Place delay",
		"The delay in ticks to wait to place a crystal after it's exploded.",
		0, 0, 20, 1);

	private final DoubleSetting placeRange = new DoubleSetting("Place range",
		"Range in which to place crystals.", 4.5, 0, 6, 0.1);

	private final IntSetting breakDelay = new IntSetting("Break delay",
		"The delay in ticks to wait to break a crystal.", 0, 0, 20, 1);

	private final DoubleSetting breakRange = new DoubleSetting("Break range",
		"Range in which to break crystals.", 4.5, 0, 6, 0.1);

	public int kaTimer;

	private LivingEntity target;
	private BlockPos bestPlacePos;
	private EndCrystal bestBreakCrystal;
	private int placeDelayLeft;
	private int breakDelayLeft;

	public CrystalAuraModule()
	{
		super("CrystalAura",
			"Automatically places and detonates end crystals to harm entities.",
			ModuleCategory.COMBAT);
		displayName("水晶光环");

		addSetting(targetRange);
		addSetting(minDamage);
		addSetting(maxSelfDamage);
		addSetting(antiSuicide);
		addSetting(rotate);
		addSetting(autoSwitch);
		addSetting(doPlace);
		addSetting(placeDelay);
		addSetting(placeRange);
		addSetting(breakDelay);
		addSetting(breakRange);

		subscribe(TickEvent.class, this::onTick);
	}

	@Override
	public void onEnable()
	{
		bestPlacePos = null;
		bestBreakCrystal = null;
		placeDelayLeft = 0;
		breakDelayLeft = 0;
		kaTimer = 0;
		target = null;
	}

	private void onTick(TickEvent event)
	{
		if(!event.isPre())
			return;

		var mc = NovaClient.MC;
		if(mc.player == null || mc.level == null)
			return;

		if(kaTimer > 0)
			kaTimer--;

		if(target == null || !target.isAlive()
			|| target.distanceToSqr(mc.player) > targetRange.get()
				* targetRange.get())
		{
			target = findTarget();
			if(target == null)
				return;
		}

		// Breaking existing crystals takes priority.
		if(findCrystalToBreak())
		{
			if(breakDelayLeft++ < breakDelay.get())
				return;

			breakCrystal(bestBreakCrystal);
			breakDelayLeft = 0;
			return;
		}

		if(autoswitchCrystals() && doPlace.isChecked())
		{
			if(findPlacePos() && placeDelayLeft++ >= placeDelay.get())
			{
				placeCrystal(bestPlacePos);
				placeDelayLeft = 0;
			}
		}
	}

	private LivingEntity findTarget()
	{
		List<Player> players = EntityUtils.getPlayers();

		return players.stream().filter(Player::isAlive)
			.min(Comparator.comparingDouble(p -> p.getHealth())).orElse(null);
	}

	private boolean autoswitchCrystals()
	{
		var mc = NovaClient.MC;
		if(!mc.player.getMainHandItem().is(Items.END_CRYSTAL)
			&& !mc.player.getOffhandItem().is(Items.END_CRYSTAL))
		{
			if(autoSwitch.get() == AutoSwitchMode.None)
				return false;

			int slot = findHotbarSlot(Items.END_CRYSTAL);
			if(slot == -1)
				return false;

			mc.player.getInventory().setSelectedSlot(slot);
		}

		return true;
	}

	private boolean findCrystalToBreak()
	{
		var mc = NovaClient.MC;
		var player = mc.player;

		bestBreakCrystal = null;
		double best = 0;

		for(EndCrystal crystal : EntityUtils.getAliveEntities(EndCrystal.class))
		{
			Vec3 pos = crystal.position();

			if(player.getEyePosition().distanceTo(pos) > breakRange.get())
				continue;

			ClipContext clip = new ClipContext(player.getEyePosition(), pos,
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
			BlockHitResult result = mc.level.clip(clip);
			if(result != null && !result.getBlockPos()
				.equals(BlockPos.containing(pos)))
				continue;

			double targetDamage = estimateDamage(pos, target, 6f);
			double selfDamage = estimateDamage(pos, player, 6f);

			if(targetDamage < minDamage.get())
				continue;

			if(antiSuicide.isChecked() && selfDamage >= player.getHealth()
				+ player.getAbsorptionAmount())
				continue;

			if(antiSuicide.isChecked() && selfDamage > maxSelfDamage.get())
				continue;

			if(targetDamage > best)
			{
				best = targetDamage;
				bestBreakCrystal = crystal;
			}
		}

		return bestBreakCrystal != null;
	}

	private boolean findPlacePos()
	{
		var mc = NovaClient.MC;
		var player = mc.player;

		bestPlacePos = null;
		double best = 0;

		int range = (int)Math.ceil(placeRange.get());
		Vec3 targetPos = target.position();

		for(int x = -range; x <= range; x++)
			for(int y = -range; y <= range; y++)
				for(int z = -range; z <= range; z++)
				{
					BlockPos pos = BlockPos.containing(targetPos).offset(x, y, z);
					BlockState state = mc.level.getBlockState(pos);

					if(!state.is(Blocks.OBSIDIAN)
						&& !state.is(Blocks.BEDROCK))
						continue;

					BlockPos above = pos.above();
					if(!mc.level.getBlockState(above).isAir())
						continue;

					if(player.getEyePosition()
						.distanceTo(Vec3.atCenterOf(above)) > placeRange.get())
						continue;

					Vec3 center = Vec3.atCenterOf(pos).add(0, 1, 0);
					double targetDamage = estimateDamage(center, target, 6f);
					double selfDamage = estimateDamage(center, player, 6f);

					if(targetDamage < minDamage.get())
						continue;

					if(antiSuicide.isChecked() && selfDamage >= player
						.getHealth() + player.getAbsorptionAmount())
						continue;

					if(antiSuicide.isChecked()
						&& selfDamage > maxSelfDamage.get())
						continue;

					if(targetDamage > best)
					{
						best = targetDamage;
						bestPlacePos = above;
					}
				}

		return bestPlacePos != null;
	}

	private void placeCrystal(BlockPos pos)
	{
		var mc = NovaClient.MC;
		IMultiPlayerGameMode gameMode = (IMultiPlayerGameMode)mc.gameMode;

		BlockPos support = pos.below();
		Direction side = Direction.UP;
		Vec3 hitVec = Vec3.atCenterOf(support).add(0, 0.5, 0);

		if(rotate.isChecked())
		{
			RotationUtils.rotate(
				RotationUtils.getYaw(mc.player.getEyePosition(), hitVec),
				RotationUtils.getPitch(mc.player.getEyePosition(), hitVec));
		}

		BlockHitResult hit = new BlockHitResult(hitVec, side, support, false);
		gameMode.nova_rightClickBlock(support, side, hitVec);
		mc.player.swing(InteractionHand.MAIN_HAND);
	}

	private void breakCrystal(EndCrystal crystal)
	{
		var mc = NovaClient.MC;

		if(rotate.isChecked())
		{
			RotationUtils.rotate(
				RotationUtils.getYaw(mc.player.getEyePosition(),
					crystal.position()),
				RotationUtils.getPitch(mc.player.getEyePosition(),
					crystal.position()));
		}

		mc.player.connection
			.send(new ServerboundAttackPacket(crystal.getId()));
		mc.player.swing(InteractionHand.MAIN_HAND);
	}

	private int findHotbarSlot(net.minecraft.world.item.Item item)
	{
		var inventory = NovaClient.MC.player.getInventory();

		for(int i = 0; i < 9; i++)
			if(inventory.getItem(i).is(item))
				return i;

		return -1;
	}

	/** Rough explosion damage estimate (used to pick a target crystal). */
	private double estimateDamage(Vec3 explosion, LivingEntity entity,
		float power)
	{
		if(entity == null)
			return 0;

		double dist = Math.sqrt(entity.distanceToSqr(explosion));
		if(dist > power * 2)
			return 0;

		double exposure = 1 - dist / (power * 2);
		double armor = entity.getArmorValue();
		double raw = (1 - dist / (power * 2)) * power * 7 * exposure;

		return Math.max(0, raw * (1 - Math.min(20, armor) / 25));
	}

	@Override
	public String getInfoString()
	{
		return target == null ? null : target.getName().getString();
	}

	public enum AutoSwitchMode
	{
		None, Normal;
	}
}
