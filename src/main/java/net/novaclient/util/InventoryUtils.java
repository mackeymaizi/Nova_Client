package net.novaclient.util;

import java.util.function.Predicate;
import java.util.stream.IntStream;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.novaclient.NovaClient;

public enum InventoryUtils
{
	;

	public static final int OFFHAND_SLOT = 40;
	public static final int OFFHAND_PKT_ID = 45;

	public static int toNetworkSlot(int slot)
	{
		// hotbar
		if(slot >= 0 && slot < 9)
			return slot + 36;

		// armor
		if(slot >= 36 && slot < 40)
			return 44 - slot;

		// offhand
		if(slot == OFFHAND_SLOT)
			return OFFHAND_PKT_ID;

		// main inventory
		return slot;
	}

	public static int count(Predicate<ItemStack> predicate, int maxInvSlot,
		boolean includeOffhand)
	{
		Inventory inventory = NovaClient.MC.player.getInventory();

		return getMatchingSlots(predicate, maxInvSlot, includeOffhand)
			.map(slot -> inventory.getItem(slot).getCount()).sum();
	}

	public static int indexOf(Predicate<ItemStack> predicate, int maxInvSlot)
	{
		return getMatchingSlots(predicate, maxInvSlot, false).findFirst()
			.orElse(-1);
	}

	public static IntStream getMatchingSlots(Predicate<ItemStack> predicate,
		int maxInvSlot, boolean includeOffhand)
	{
		Inventory inventory = NovaClient.MC.player.getInventory();

		IntStream stream = IntStream.range(0, maxInvSlot);
		if(includeOffhand)
			stream = IntStream.concat(stream, IntStream.of(OFFHAND_SLOT));

		return stream.filter(i -> predicate.test(inventory.getItem(i)));
	}
}
