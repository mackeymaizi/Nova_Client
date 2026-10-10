/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.IntSetting;
import net.novaclient.setting.StringListSetting;
import net.novaclient.util.InventoryUtils;

/** Ported from Wurst's RestockHack. */
public final class RestockModule extends Module
{
	private static final List<Integer> SEARCH_SLOTS =
		Stream.concat(IntStream.range(0, 36).boxed(),
			Stream.of(InventoryUtils.OFFHAND_SLOT))
			.collect(java.util.stream.Collectors
				.toCollection(ArrayList::new));

	private final StringListSetting items = new StringListSetting("Items",
		"Item(s) to be restocked.", "minecraft:minecart");

	private final IntSetting restockSlot = new IntSetting("Slot",
		"To which slot should we restock.\n-1 = current slot, 9 = offhand", 0,
		-1, 9, 1);

	private final IntSetting restockAmount = new IntSetting("Minimum amount",
		"Minimum amount of items in hand before a new round of restocking is"
			+ " triggered.",
		1, 1, 64, 1);

	private final IntSetting repairMode = new IntSetting("Tools repair mode",
		"Swaps out tools when their durability reaches the given threshold,"
			+ " so you can repair them before they break.\nCan be adjusted"
			+ " from 0 (off) to 100 remaining uses.",
		0, 0, 100, 1);

	public RestockModule()
	{
		super("Restock", "Automatically refills your hotbar with the items"
			+ " you need.", ModuleCategory.MISC);
		displayName("自动补货");

		addSetting(items);
		addSetting(restockSlot);
		addSetting(restockAmount);
		addSetting(repairMode);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	private void onUpdate()
	{
		// Don't mess with the inventory while it's open.
		if(NovaClient.MC.gui.screen() instanceof AbstractContainerScreen)
			return;

		var inv = NovaClient.MC.player.getInventory();
		var im = NovaClient.IMC.nova_getInteractionManager();

		int hotbarSlot = restockSlot.get();
		if(hotbarSlot == -1)
			hotbarSlot = inv.getSelectedSlot();
		else if(hotbarSlot == 9)
			hotbarSlot = InventoryUtils.OFFHAND_SLOT;

		for(String itemName : items.get())
		{
			ItemStack hotbarStack = inv.getItem(hotbarSlot);

			boolean wrongItem =
				hotbarStack.isEmpty() || !itemEqual(itemName, hotbarStack);
			if(!wrongItem && hotbarStack.getCount() >= Math
				.min(restockAmount.get(), hotbarStack.getMaxStackSize()))
				return;

			List<Integer> searchResult =
				searchSlotsWithItem(itemName, hotbarSlot);
			for(int itemIndex : searchResult)
			{
				int pickupIndex = InventoryUtils.toNetworkSlot(itemIndex);

				im.nova_windowClick_PICKUP(pickupIndex);
				im.nova_windowClick_PICKUP(
					InventoryUtils.toNetworkSlot(hotbarSlot));
				if(!NovaClient.MC.player.inventoryMenu.getCarried().isEmpty())
					im.nova_windowClick_PICKUP(pickupIndex);

				if(hotbarStack.getCount() >= hotbarStack.getMaxStackSize())
					break;
			}

			if(wrongItem && searchResult.isEmpty())
				continue;

			break;
		}

		ItemStack restockStack = inv.getItem(hotbarSlot);
		if(repairMode.get() > 0 && restockStack.isDamageableItem()
			&& isTooDamaged(restockStack))
			for(int i : SEARCH_SLOTS)
			{
				if(i == hotbarSlot || i == InventoryUtils.OFFHAND_SLOT)
					continue;

				ItemStack stack = inv.getItem(i);
				if(stack.isEmpty() || !stack.isDamageableItem())
				{
					NovaClient.IMC.nova_getInteractionManager()
						.nova_windowClick_SWAP(i,
							InventoryUtils.toNetworkSlot(hotbarSlot));
					break;
				}
			}
	}

	private boolean isTooDamaged(ItemStack stack)
	{
		return stack.getMaxDamage()
			- stack.getDamageValue() <= repairMode.get();
	}

	private List<Integer> searchSlotsWithItem(String itemName, int slotToSkip)
	{
		List<Integer> slots = new ArrayList<>();

		for(int i : SEARCH_SLOTS)
		{
			if(i == slotToSkip)
				continue;

			ItemStack stack = NovaClient.MC.player.getInventory().getItem(i);
			if(stack.isEmpty())
				continue;

			if(itemEqual(itemName, stack))
				slots.add(i);
		}

		return slots;
	}

	private boolean itemEqual(String itemName, ItemStack stack)
	{
		if(repairMode.get() > 0 && stack.isDamageableItem()
			&& isTooDamaged(stack))
			return false;

		return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()
			.equals(itemName);
	}
}
