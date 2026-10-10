/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.misc;

import java.util.OptionalInt;
import java.util.stream.IntStream;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.novaclient.NovaClient;
import net.novaclient.event.events.BlockBreakingProgressEvent;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.IntSetting;
import net.novaclient.util.BlockUtils;
import net.novaclient.util.InventoryUtils;

/** Ported from Wurst's AutoToolHack. */
public final class AutoToolModule extends Module
{
	private final BoolSetting useSwords = new BoolSetting("Use swords",
		"Uses swords to break leaves, cobwebs, etc.", false);

	private final BoolSetting useHands = new BoolSetting("Use hands",
		"Uses an empty hand or a non-damageable item when no applicable tool"
			+ " is found.",
		true);

	private final IntSetting repairMode = new IntSetting("Repair mode",
		"Prevents tools from being used when their durability reaches the"
			+ " given threshold, so you can repair them before they break."
			+ "\nCan be adjusted from 0 (off) to 100 remaining uses.",
		0, 0, 100, 1);

	private final BoolSetting switchBack = new BoolSetting("Switch back",
		"After using a tool, automatically switches back to the previously"
			+ " selected slot.",
		false);

	private int prevSelectedSlot = -1;

	public AutoToolModule()
	{
		super("AutoTool", "Automatically equips the best tool for breaking"
			+ " blocks.", ModuleCategory.MISC);
		displayName("自动换工具");

		addSetting(useSwords);
		addSetting(useHands);
		addSetting(repairMode);
		addSetting(switchBack);

		subscribe(BlockBreakingProgressEvent.class, e -> onBreaking(e));
		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	private void onBreaking(BlockBreakingProgressEvent event)
	{
		BlockPos pos = event.blockPos();
		if(!BlockUtils.canBeClicked(pos))
			return;

		if(prevSelectedSlot == -1)
			prevSelectedSlot =
				NovaClient.MC.player.getInventory().getSelectedSlot();

		equipBestTool(pos, useSwords.isChecked(), useHands.isChecked(),
			repairMode.get());
	}

	private void onUpdate()
	{
		if(prevSelectedSlot == -1 || NovaClient.MC.gameMode.isDestroying())
			return;

		HitResult hitResult = NovaClient.MC.hitResult;
		if(hitResult != null && hitResult.getType() == HitResult.Type.BLOCK)
			return;

		if(switchBack.isChecked())
			NovaClient.MC.player.getInventory()
				.setSelectedSlot(prevSelectedSlot);

		prevSelectedSlot = -1;
	}

	public void equipBestTool(BlockPos pos, boolean useSwords, boolean useHands,
		int repairMode)
	{
		var player = NovaClient.MC.player;
		if(player.getAbilities().instabuild)
			return;

		ItemStack heldItem = player.getMainHandItem();
		boolean heldItemDamageable = isDamageable(heldItem);
		if(heldItemDamageable && isTooDamaged(heldItem, repairMode))
			putAwayDamagedTool(repairMode);

		BlockState state = BlockUtils.getState(pos);
		int bestSlot = getBestSlot(state, useSwords, repairMode);
		if(bestSlot == -1)
		{
			if(useHands && heldItemDamageable && isWrongTool(heldItem, state))
				selectFallbackSlot();

			return;
		}

		player.getInventory().setSelectedSlot(bestSlot);
	}

	private int getBestSlot(BlockState state, boolean useSwords, int repairMode)
	{
		var inventory = NovaClient.MC.player.getInventory();
		ItemStack heldItem = NovaClient.MC.player.getMainHandItem();

		float bestSpeed = getMiningSpeed(heldItem, state);
		if(isTooDamaged(heldItem, repairMode))
			bestSpeed = 1;

		int bestSlot = -1;
		for(int slot = 0; slot < 9; slot++)
		{
			if(slot == inventory.getSelectedSlot())
				continue;

			ItemStack stack = inventory.getItem(slot);

			float speed = getMiningSpeed(stack, state);
			if(speed <= bestSpeed)
				continue;

			if(!useSwords && stack.is(ItemTags.SWORDS))
				continue;

			if(isTooDamaged(stack, repairMode))
				continue;

			bestSpeed = speed;
			bestSlot = slot;
		}

		return bestSlot;
	}

	private float getMiningSpeed(ItemStack stack, BlockState state)
	{
		return stack.getDestroySpeed(state);
	}

	private boolean isDamageable(ItemStack stack)
	{
		return !stack.isEmpty() && stack.isDamageableItem();
	}

	private boolean isTooDamaged(ItemStack stack, int repairMode)
	{
		return stack.getMaxDamage() - stack.getDamageValue() <= repairMode;
	}

	private void putAwayDamagedTool(int repairMode)
	{
		var inv = NovaClient.MC.player.getInventory();
		int selectedSlot = inv.getSelectedSlot();
		var im = NovaClient.IMC.nova_getInteractionManager();

		OptionalInt emptySlot = IntStream.range(9, 36)
			.filter(i -> inv.getItem(i).isEmpty()).findFirst();
		if(emptySlot.isPresent())
		{
			im.nova_windowClick_QUICK_MOVE(
				InventoryUtils.toNetworkSlot(selectedSlot));
			return;
		}

		OptionalInt nonDamageableSlot = IntStream.range(9, 36)
			.filter(i -> !isDamageable(inv.getItem(i))).findFirst();
		if(nonDamageableSlot.isPresent())
		{
			im.nova_windowClick_SWAP(nonDamageableSlot.getAsInt(),
				selectedSlot);
			return;
		}

		OptionalInt notTooDamagedSlot = IntStream.range(9, 36)
			.filter(i -> !isTooDamaged(inv.getItem(i), repairMode))
			.findFirst();
		if(notTooDamagedSlot.isPresent())
		{
			im.nova_windowClick_SWAP(notTooDamagedSlot.getAsInt(),
				selectedSlot);
			return;
		}

		im.nova_windowClick_SWAP(0, selectedSlot);
	}

	private boolean isWrongTool(ItemStack heldItem, BlockState state)
	{
		return getMiningSpeed(heldItem, state) <= 1;
	}

	private void selectFallbackSlot()
	{
		var inventory = NovaClient.MC.player.getInventory();
		int fallbackSlot = getFallbackSlot();

		if(fallbackSlot == -1)
		{
			int prevSlot = inventory.getSelectedSlot();
			inventory.setSelectedSlot(prevSlot == 8 ? 0 : prevSlot + 1);
			return;
		}

		inventory.setSelectedSlot(fallbackSlot);
	}

	private int getFallbackSlot()
	{
		var inventory = NovaClient.MC.player.getInventory();

		for(int slot = 0; slot < 9; slot++)
		{
			if(slot == inventory.getSelectedSlot())
				continue;

			if(!isDamageable(inventory.getItem(slot)))
				return slot;
		}

		return -1;
	}
}
