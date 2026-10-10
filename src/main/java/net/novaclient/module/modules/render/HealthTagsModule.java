/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;

/** Ported from Wurst's HealthTagsHack. */
public final class HealthTagsModule extends Module
{
	public HealthTagsModule()
	{
		super("HealthTags", "Adds the health of a mob to its nametag.",
			ModuleCategory.RENDER);
		displayName("血量标签");
	}

	public Component addHealth(LivingEntity entity, MutableComponent nametag)
	{
		if(!isEnabled())
			return nametag;

		int health = (int)entity.getHealth();

		MutableComponent formattedHealth = Component.literal(" ")
			.append(Integer.toString(health)).withStyle(getColor(health));

		return nametag.append(formattedHealth);
	}

	private ChatFormatting getColor(int health)
	{
		if(health <= 5)
			return ChatFormatting.DARK_RED;

		if(health <= 10)
			return ChatFormatting.GOLD;

		if(health <= 15)
			return ChatFormatting.YELLOW;

		return ChatFormatting.GREEN;
	}
}
