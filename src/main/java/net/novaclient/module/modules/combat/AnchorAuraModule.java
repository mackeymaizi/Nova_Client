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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
import net.novaclient.util.ChatUtils;
import net.novaclient.util.EntityUtils;
import net.novaclient.util.InventoryUtils;
import net.novaclient.util.RotationUtils;

/**
 * Ported from Meteor's AnchorAura. Places and detonates respawn anchors
 * near the target.
 */
public final class AnchorAuraModule extends Module
{
	private final DoubleSetting targetRange = new DoubleSetting("Target range",
		"Range in which to target players.", 10, 0, 16, 0.5);

	private final DoubleSetting minDamage = new DoubleSetting("Min damage",
		"The minimum damage to inflict on your target.", 7, 0, 36, 0.5);

	private final DoubleSetting maxSelfDamage = new DoubleSetting(
		"Max self damage", "The maximum damage to inflict on yourself.", 7, 0,
		36, 0.5);

	private final BoolSetting antiSuicide = new BoolSetting("Anti suicide",
		"Will not place and break anchors if they will kill you.", true);

	private final BoolSetting swapBack = new BoolSetting("Swap back",
		"Switches back to your previous slot after using anchors.", true);

	private final BoolSetting rotate = new BoolSetting("Rotate",
		"Rotates towards the anchors being placed/broken.", true);

	private final BoolSetting place = new BoolSetting("Place",
		"Allows Anchor Aura to place anchors.", true);

	private final IntSetting placeDelay = new IntSetting("Place delay",
		"The tick delay between placing anchors.", 5, 0, 10, 1);

	private final DoubleSetting placeRange = new DoubleSetting("Place range",
		"The range at which anchors can be placed.", 4, 0, 6, 0.1);

	private final IntSetting chargeDelay = new IntSetting("Charge delay",
		"The tick delay it takes to charge anchors.", 1, 0, 10, 1);

	private final IntSetting breakDelay = new IntSetting("Break delay",
		"The tick delay it takes to break anchors.", 1, 0, 10, 1);

	private final DoubleSetting breakRange = new DoubleSetting("Break range",
		"Range in which to break anchors.", 4.5, 0, 6, 0.1);

	private final DoubleSetting wallsRange = new DoubleSetting("Walls range",
		"Range at which anchors can be placed/broken through walls.", 3.5, 0, 6,
		0.1);

	private final BoolSetting airPlace = new BoolSetting("Air place",
		"Allows placing anchors in mid-air.", false);

	private final BoolSetting pauseOnUse = new BoolSetting("Pause on use",
		"Pauses while you are using an item.", true);

	private final BoolSetting pauseOnMine = new BoolSetting("Pause on mine",
		"Pauses while you are mining a block.", true);

	private final BoolSetting pauseOnCA = new BoolSetting("Pause on CA",
		"Pauses while CrystalAura is enabled.", false);

	private final BoolSetting swing = new BoolSetting("Swing",
		"Swing your hand when interacting.", true);

	private final EnumSetting<TargetPriority> targetPriority = new EnumSetting<>(
		"Target priority", "Which target to attack first.",
		TargetPriority.values(), TargetPriority.LowestHealth);

	private BlockPos bestPlacePos;
	private double bestPlaceDamage;
	private BlockPos bestBreakPos;
	private double bestBreakDamage;

	private LivingEntity target;
	private int placeDelayLeft;
	private int chargeDelayLeft;
	private int breakDelayLeft;

	public AnchorAuraModule()
	{
		super("AnchorAura",
			"Automatically places and breaks respawn anchors to harm entities.",
			ModuleCategory.COMBAT);
		displayName("重生锚光环");

		addSetting(targetRange);
		addSetting(minDamage);
		addSetting(maxSelfDamage);
		addSetting(antiSuicide);
		addSetting(swapBack);
		addSetting(rotate);
		addSetting(place);
		addSetting(placeDelay);
		addSetting(placeRange);
		addSetting(chargeDelay);
		addSetting(breakDelay);
		addSetting(breakRange);
		addSetting(wallsRange);
		addSetting(airPlace);
		addSetting(pauseOnUse);
		addSetting(pauseOnMine);
		addSetting(pauseOnCA);
		addSetting(swing);
		addSetting(targetPriority);

		subscribe(TickEvent.class, this::onTick);
	}

	@Override
	public void onEnable()
	{
		bestPlacePos = null;
		bestBreakPos = null;
		placeDelayLeft = placeDelay.get();
		chargeDelayLeft = 0;
		breakDelayLeft = 0;
		target = null;
	}

	private void onTick(TickEvent event)
	{
		if(!event.isPre())
			return;

		var mc = NovaClient.MC;
		if(mc.player == null || mc.level == null)
			return;

		if(mc.level.dimension() == Level.NETHER)
		{
			ChatUtils.error(
				"You can't use respawn anchors in the Nether; disabling"
					+ " AnchorAura.");
			setEnabled(false);
			return;
		}

		if(mc.player.isUsingItem() || mc.gameMode.isDestroying())
			return;

		if(pauseOnUse.isChecked() && mc.player.isUsingItem())
			return;

		if(pauseOnMine.isChecked() && mc.gameMode.isDestroying())
			return;

		if(pauseOnCA.isChecked())
		{
			var crystalAura = net.novaclient.module.ModuleManager
				.get(CrystalAuraModule.class);
			if(crystalAura != null && crystalAura.isEnabled())
				return;
		}

		// Find a target
		if(target == null || !target.isAlive()
			|| target.distanceToSqr(mc.player) > targetRange.get()
				* targetRange.get())
		{
			target = findTarget();
			if(target == null)
				return;
		}

		findAnchors();
	}

	private LivingEntity findTarget()
	{
		var mc = NovaClient.MC;
		List<Player> players = EntityUtils.getPlayers();

		Comparator<Player> cmp = switch(targetPriority.get())
		{
			case Closest -> Comparator
				.comparingDouble(p -> p.distanceToSqr(mc.player));
			case LowestHealth -> Comparator.comparingDouble(Player::getHealth);
		};

		return players.stream().filter(Player::isAlive).min(cmp).orElse(null);
	}

	private void findAnchors()
	{
		var mc = NovaClient.MC;
		var player = mc.player;

		bestPlacePos = null;
		bestBreakPos = null;
		bestPlaceDamage = 0;
		bestBreakDamage = 0;

		int range = (int)Math.ceil(Math.max(placeRange.get(), breakRange.get()));
		BlockPos origin = player.blockPosition();

		for(BlockPos pos : BlockPos.betweenClosed(origin.offset(-range, -range,
			-range), origin.offset(range, range, range)))
		{
			BlockState state = mc.level.getBlockState(pos);
			boolean isAnchor = state.is(Blocks.RESPAWN_ANCHOR);
			boolean isPlacing = !isAnchor;

			if(isPlacing && (!place.isChecked()
				|| !state.canBeReplaced()))
				continue;

			double baseRange = isPlacing ? placeRange.get() : breakRange.get();
			Vec3 center = Vec3.atCenterOf(pos);

			if(player.getEyePosition().distanceTo(center) > baseRange)
				continue;

			// Raycast visibility check
			ClipContext clip = new ClipContext(player.getEyePosition(), center,
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
			BlockHitResult result = mc.level.clip(clip);
			if(result == null || !result.getBlockPos().equals(pos))
			{
				// Not directly visible - only allowed within the walls range.
				if(player.getEyePosition().distanceTo(center) > wallsRange.get())
					continue;
			}

			if(isPlacing && !airPlace.isChecked())
			{
				// Require a solid neighbour to place against.
				boolean hasNeighbour = false;
				for(Direction dir : Direction.values())
					if(!mc.level.getBlockState(pos.relative(dir)).isAir())
					{
						hasNeighbour = true;
						break;
					}

				if(!hasNeighbour)
					continue;
			}

			// Very rough damage estimate (explosion radius 5).
			double targetDamage = estimateDamage(center, target, 5f);
			double selfDamage = estimateDamage(center, player, 5f);

			if(targetDamage < minDamage.get())
				continue;

			if(antiSuicide.isChecked() && selfDamage >= player.getHealth()
				+ player.getAbsorptionAmount())
				continue;

			if(antiSuicide.isChecked() && selfDamage > maxSelfDamage.get())
				continue;

			if(isPlacing)
			{
				if(targetDamage > bestPlaceDamage)
				{
					bestPlaceDamage = targetDamage;
					bestPlacePos = pos.immutable();
				}
			}else if(targetDamage > bestBreakDamage)
			{
				bestBreakDamage = targetDamage;
				bestBreakPos = pos.immutable();
			}
		}

		if(bestBreakDamage > 0)
			doBreak(bestBreakPos);
		else if(bestPlaceDamage > 0 && bestPlacePos != null)
			doPlace(bestPlacePos);
	}

	private void doPlace(BlockPos pos)
	{
		var mc = NovaClient.MC;

		if(placeDelayLeft++ < placeDelay.get())
			return;

		int anchorSlot = findHotbarSlot(Items.RESPAWN_ANCHOR);
		if(anchorSlot == -1)
			return;

		int previous = mc.player.getInventory().getSelectedSlot();
		mc.player.getInventory().setSelectedSlot(anchorSlot);

		if(rotate.isChecked())
			RotationUtils.rotate(RotationUtils.getYaw(mc.player.getEyePosition(),
				Vec3.atCenterOf(pos)), RotationUtils.getPitch(
					mc.player.getEyePosition(), Vec3.atCenterOf(pos)));

		placeBlock(pos);

		if(swapBack.isChecked())
			mc.player.getInventory().setSelectedSlot(previous);

		placeDelayLeft = 0;
	}

	private void doBreak(BlockPos pos)
	{
		var mc = NovaClient.MC;
		BlockState state = mc.level.getBlockState(pos);

		if(!state.is(Blocks.RESPAWN_ANCHOR))
			return;

		Vec3 center = Vec3.atCenterOf(pos);
		int charges = state.getValue(BlockStateProperties.RESPAWN_ANCHOR_CHARGES);

		int previous = mc.player.getInventory().getSelectedSlot();

		if(charges == 0)
		{
			if(chargeDelayLeft++ < chargeDelay.get())
				return;

			int glowSlot = findHotbarSlot(Items.GLOWSTONE);
			if(glowSlot == -1)
				return;

			mc.player.getInventory().setSelectedSlot(glowSlot);
			interactBlock(pos, center);
			chargeDelayLeft = 0;
		}else
		{
			if(breakDelayLeft++ < breakDelay.get())
				return;

			int otherSlot = findHotbarSlotExcept(Items.GLOWSTONE);
			if(otherSlot == -1)
				return;

			mc.player.getInventory().setSelectedSlot(otherSlot);
			interactBlock(pos, center);
			breakDelayLeft = 0;

			// Predict the explosion client-side.
			mc.level.setBlock(pos,
				mc.level.getFluidState(pos).createLegacyBlock(), 0);
		}

		if(swapBack.isChecked())
			mc.player.getInventory().setSelectedSlot(previous);
	}

	private void placeBlock(BlockPos pos)
	{
		var mc = NovaClient.MC;
		IMultiPlayerGameMode gameMode = (IMultiPlayerGameMode)mc.gameMode;

		Direction side = getPlaceSide(pos);
		Vec3 hitVec = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(
			side.getUnitVec3i()).scale(0.5));

		BlockHitResult hit = new BlockHitResult(hitVec, side, pos, false);
		gameMode.nova_rightClickBlock(pos, side, hit.getLocation());
		if(swing.isChecked())
			mc.player.swing(InteractionHand.MAIN_HAND);
	}

	private void interactBlock(BlockPos pos, Vec3 center)
	{
		var mc = NovaClient.MC;
		Direction side = Direction.UP;
		BlockHitResult hit = new BlockHitResult(center, side, pos, false);

		mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
		mc.player.swing(InteractionHand.MAIN_HAND);
	}

	private Direction getPlaceSide(BlockPos pos)
	{
		var mc = NovaClient.MC;
		Direction best = Direction.UP;
		double bestDist = Double.MAX_VALUE;
		Vec3 eyes = mc.player.getEyePosition();

		for(Direction dir : Direction.values())
		{
			BlockPos neighbor = pos.relative(dir);
			if(mc.level.getBlockState(neighbor).isAir())
				continue;

			double dist = eyes.distanceToSqr(Vec3.atCenterOf(neighbor));
			if(dist < bestDist)
			{
				bestDist = dist;
				best = dir.getOpposite();
			}
		}

		return best;
	}

	/** Finds the first hotbar slot containing the given item. */
	private int findHotbarSlot(net.minecraft.world.item.Item item)
	{
		var inventory = NovaClient.MC.player.getInventory();

		for(int i = 0; i < 9; i++)
			if(inventory.getItem(i).is(item))
				return i;

		return -1;
	}

	/** Finds the first non-empty hotbar slot that is not the given item. */
	private int findHotbarSlotExcept(net.minecraft.world.item.Item skip)
	{
		var inventory = NovaClient.MC.player.getInventory();

		for(int i = 0; i < 9; i++)
		{
			var stack = inventory.getItem(i);
			if(!stack.isEmpty() && !stack.is(skip))
				return i;
		}

		return -1;
	}

	/** Rough explosion damage estimate (used to pick a target block). */
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

	public enum TargetPriority
	{
		LowestHealth, Closest;
	}
}
