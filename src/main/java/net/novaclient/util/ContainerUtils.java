/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.novaclient.NovaClient;

/** Opens fake, client-side container screens (used by Peek / EnderChest). */
public enum ContainerUtils
{
	;

	/** Opens a 27-slot read-only preview of the given items. */
	public static void openPreview(ItemStack[] items, String title)
	{
		SimpleContainer container = new SimpleContainer(27);

		for(int i = 0; i < 27; i++)
			container.setItem(i,
				items != null && i < items.length && items[i] != null
					? items[i] : ItemStack.EMPTY);

		ChestMenu menu = ChestMenu.threeRows(0,
			NovaClient.MC.player.getInventory(), container);

		NovaClient.MC.setScreen(
			new net.minecraft.client.gui.screens.inventory.ContainerScreen(menu,
				NovaClient.MC.player.getInventory(),
				Component.literal(title)));
	}
}
