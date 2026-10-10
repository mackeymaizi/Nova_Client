/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.novaclient.command.Command;
import net.novaclient.util.InventoryUtils;

/** {@code .drop <hand|offhand|hotbar|inventory|all>} - drops items. */
public final class DropCommand extends Command
{
	private static final SimpleCommandExceptionType NOT_SPECTATOR =
		new SimpleCommandExceptionType(
			Component.literal("Can't drop items while in spectator."));

	public DropCommand()
	{
		super("drop", "Automatically drops specified items.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(literal("hand").executes(ctx -> dropHand(-1)));
		builder.then(literal("offhand")
			.executes(ctx -> dropHand(InventoryUtils.OFFHAND_SLOT)));
		builder.then(literal("hotbar").executes(ctx -> {
			for(int i = 0; i < 9; i++)
				dropSlot(i);
			return SINGLE_SUCCESS;
		}));
		builder.then(literal("inventory").executes(ctx -> {
			for(int i = 9; i < 36; i++)
				dropSlot(i);
			return SINGLE_SUCCESS;
		}));
		builder.then(literal("all").executes(ctx -> {
			for(int i = 0; i < 36; i++)
				dropSlot(i);
			if(!mc().player.getOffhandItem().isEmpty())
				dropSlot(InventoryUtils.OFFHAND_SLOT);
			return SINGLE_SUCCESS;
		}));
	}

	private int dropHand(int slot) throws com.mojang.brigadier.exceptions.CommandSyntaxException
	{
		if(mc().player.isSpectator())
			throw NOT_SPECTATOR.create();

		if(slot < 0)
			dropSlot(mc().player.getInventory().getSelectedSlot());
		else
			dropSlot(slot);

		return SINGLE_SUCCESS;
	}

	private void dropSlot(int slot)
	{
		var inventory = mc().player.getInventory();
		ItemStack stack = inventory.getItem(slot);

		if(stack.isEmpty())
			return;

		inventory.setItem(slot, ItemStack.EMPTY);
		mc().player.drop(stack, false);
	}
}
