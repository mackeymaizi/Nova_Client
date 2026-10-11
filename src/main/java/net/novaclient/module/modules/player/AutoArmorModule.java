/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.player;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.mixininterface.IMultiPlayerGameMode;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.IntSetting;
import net.novaclient.util.InventoryUtils;

/** Ported from Wurst's AutoArmorHack. Equips the best armour you carry. */
public final class AutoArmorModule extends Module
{
	private static final EquipmentSlot[] ARMOR_SLOTS = {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
		EquipmentSlot.FEET
	};

	private final IntSetting delay = new IntSetting("Delay",
		"Ticks between armour moves.", 2, 0, 20, 1);

	private final BoolSetting swapIfBetter = new BoolSetting("Swap if better",
		"Replaces your current armour when a better piece is found.", true);

	private final BoolSetting useEnchantments = new BoolSetting("Use enchantments",
		"Considers the Protection enchantment when scoring armour.", true);

	private final BoolSetting swapWhileMoving = new BoolSetting("Swap while moving",
		"Also swaps armour while you are moving.", true);

	private int timer;

	public AutoArmorModule()
	{
		super("AutoArmor", "Automatically equips the best armour you have.",
			ModuleCategory.PLAYER);
		displayName("自动穿甲");

		addSetting(delay);
		addSetting(swapIfBetter);
		addSetting(useEnchantments);
		addSetting(swapWhileMoving);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player == null || player.isCreative())
			return;

		if(!swapWhileMoving.isChecked()
			&& player.input.getMoveVector().lengthSquared() > 1.0E-5F)
			return;

		if(timer > 0)
		{
			timer--;
			return;
		}

		for(EquipmentSlot slot : ARMOR_SLOTS)
			if(equipBest(slot))
			{
				timer = delay.get();
				return;
			}
	}

	private boolean equipBest(EquipmentSlot slot)
	{
		var inventory = NovaClient.MC.player.getInventory();
		int armorInvSlot = armorInvSlot(slot);
		ItemStack current = NovaClient.MC.player.getItemBySlot(slot);

		int bestSlot = -1;
		double bestScore = swapIfBetter.isChecked() ? score(current, slot) : -1;

		for(int i = 0; i < 36; i++)
		{
			ItemStack stack = inventory.getItem(i);

			if(!isArmorFor(stack, slot))
				continue;

			double score = score(stack, slot);
			if(score > bestScore)
			{
				bestScore = score;
				bestSlot = i;
			}
		}

		if(bestSlot == -1)
			return false;

		var imc = (IMultiPlayerGameMode)NovaClient.MC.gameMode;

		// Pick the item up, then place it into the armour slot.
		imc.nova_windowClick_PICKUP(InventoryUtils.toNetworkSlot(bestSlot));
		imc.nova_windowClick_PICKUP(armorInvSlot);

		// If something was already worn, put it back into the inventory.
		if(!current.isEmpty())
			imc.nova_windowClick_PICKUP(InventoryUtils.toNetworkSlot(bestSlot));

		return true;
	}

	private boolean isArmorFor(ItemStack stack, EquipmentSlot slot)
	{
		if(stack.isEmpty())
			return false;

		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		return equippable != null && equippable.slot() == slot;
	}

	/** Inventory index for an armour slot (36..39 in the player inventory). */
	private int armorInvSlot(EquipmentSlot slot)
	{
		return switch(slot)
		{
			case FEET -> 36;
			case LEGS -> 37;
			case CHEST -> 38;
			case HEAD -> 39;
			default -> -1;
		};
	}

	/** Armour value + a small toughness bonus (+ protection enchantments). */
	private double score(ItemStack stack, EquipmentSlot slot)
	{
		if(stack.isEmpty())
			return -1;

		ItemAttributeModifiers modifiers =
			stack.get(DataComponents.ATTRIBUTE_MODIFIERS);

		if(modifiers == null)
			return -1;

		double score = modifiers.compute(Attributes.ARMOR, 0, slot)
			+ modifiers.compute(Attributes.ARMOR_TOUGHNESS, 0, slot) * 0.1;

		if(useEnchantments.isChecked())
			score += enchantmentBonus(stack);

		return score;
	}

	/** Sums the levels of all enchantments; a cheap proxy for Protection. */
	private double enchantmentBonus(ItemStack stack)
	{
		var enchantments = stack.get(net.minecraft.core.component.DataComponents
			.ENCHANTMENTS);

		if(enchantments == null || enchantments.isEmpty())
			return 0;

		int total = 0;
		for(var entry : enchantments.entrySet())
			total += entry.getIntValue();

		return total * 0.5;
	}
}
