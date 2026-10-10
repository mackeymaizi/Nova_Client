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
import net.novaclient.util.ContainerUtils;

/** {@code .peek} - looks inside the container you are holding / looking at. */
public final class PeekCommand extends Command
{
	private static final SimpleCommandExceptionType CANT_PEEK =
		new SimpleCommandExceptionType(Component.literal(
			"You must be holding a storage block or looking at an item frame."));

	public PeekCommand()
	{
		super("peek", "Lets you see what's inside storage block items.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.executes(ctx -> {
			ItemStack main = mc().player.getMainHandItem();
			if(isContainer(main))
				return peek(main);

			ItemStack off = mc().player.getOffhandItem();
			if(isContainer(off))
				return peek(off);

			if(mc().crosshairPickEntity instanceof net.minecraft.world.entity.decoration.ItemFrame frame
				&& isContainer(frame.getItem()))
				return peek(frame.getItem());

			throw CANT_PEEK.create();
		});
	}

	private boolean isContainer(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;

		var component = stack
			.get(net.minecraft.core.component.DataComponents.CONTAINER);

		return component != null;
	}

	private int peek(ItemStack stack)
	{
		var component = stack
			.get(net.minecraft.core.component.DataComponents.CONTAINER);
		ItemStack[] items = new ItemStack[27];

		if(component != null)
		{
			int i = 0;
			for(ItemStack item : component.nonEmptyItems())
			{
				if(i >= 27)
					break;
				items[i++] = item;
			}
		}

		ContainerUtils.openPreview(items, stack.getHoverName().getString());
		return SINGLE_SUCCESS;
	}
}
