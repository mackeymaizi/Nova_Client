/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.novaclient.command.Command;

/**
 * {@code .give <item> [amount]} - spawns an item into your inventory.
 * Only works in creative mode on servers that accept the creative inventory.
 */
public final class GiveCommand extends Command
{
	public GiveCommand()
	{
		super("give", "Gives you an item.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(argument("item", StringArgumentType.word())
			.suggests((ctx, b) -> net.minecraft.commands.SharedSuggestionProvider
				.suggest(BuiltInRegistries.ITEM.keySet().stream()
					.map(Identifier::toString), b))
			.executes(ctx -> give(ctx.getArgument("item", String.class), 1))
			.then(argument("amount", IntegerArgumentType.integer(1, 64))
				.executes(ctx -> give(ctx.getArgument("item", String.class),
					IntegerArgumentType.getInteger(ctx, "amount")))));
	}

	private int give(String name, int amount)
	{
		Identifier id = Identifier.withDefaultNamespace(name);
		Item item = BuiltInRegistries.ITEM.getValue(id);

		if(item == null)
		{
			error("Unknown item \"%s\".", name);
			return SINGLE_SUCCESS;
		}

		ItemStack stack = new ItemStack(item, amount);
		if(!mc().player.getInventory().add(stack))
			mc().player.drop(stack, false);

		info("Gave %d x %s.", amount, name);
		return SINGLE_SUCCESS;
	}
}
