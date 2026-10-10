/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.item.ItemStack;
import net.novaclient.command.Command;
import net.novaclient.util.ContainerUtils;

/** {@code .enderchest} - previews your remembered ender chest contents. */
public final class EnderChestCommand extends Command
{
	public EnderChestCommand()
	{
		super("enderchest", "Previews your ender chest.", "ec", "echest");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.executes(ctx -> {
			var enderChest = mc().player.getEnderChestInventory();
			ItemStack[] items = new ItemStack[27];

			for(int i = 0; i < 27; i++)
				items[i] = enderChest.getItem(i);

			ContainerUtils.openPreview(items, "Ender Chest");
			return SINGLE_SUCCESS;
		});
	}
}
