/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import java.util.Map;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.novaclient.command.Command;

/** {@code .enchant <enchantment> [level]} - enchants your held item. */
public final class EnchantCommand extends Command
{
	private static final Map<String, ResourceKey<Enchantment>> NAMES = Map.ofEntries(
		Map.entry("sharpness", Enchantments.SHARPNESS),
		Map.entry("smite", Enchantments.SMITE),
		Map.entry("bane_of_arthropods", Enchantments.BANE_OF_ARTHROPODS),
		Map.entry("knockback", Enchantments.KNOCKBACK),
		Map.entry("fire_aspect", Enchantments.FIRE_ASPECT),
		Map.entry("looting", Enchantments.LOOTING),
		Map.entry("sweeping_edge", Enchantments.SWEEPING_EDGE),
		Map.entry("efficiency", Enchantments.EFFICIENCY),
		Map.entry("unbreaking", Enchantments.UNBREAKING),
		Map.entry("fortune", Enchantments.FORTUNE),
		Map.entry("silk_touch", Enchantments.SILK_TOUCH),
		Map.entry("power", Enchantments.POWER),
		Map.entry("punch", Enchantments.PUNCH),
		Map.entry("flame", Enchantments.FLAME),
		Map.entry("infinity", Enchantments.INFINITY),
		Map.entry("protection", Enchantments.PROTECTION),
		Map.entry("feather_falling", Enchantments.FEATHER_FALLING),
		Map.entry("thorns", Enchantments.THORNS),
		Map.entry("mending", Enchantments.MENDING),
		Map.entry("respiration", Enchantments.RESPIRATION),
		Map.entry("depth_strider", Enchantments.DEPTH_STRIDER),
		Map.entry("aqua_affinity", Enchantments.AQUA_AFFINITY),
		Map.entry("loyalty", Enchantments.LOYALTY),
		Map.entry("riptide", Enchantments.RIPTIDE),
		Map.entry("channeling", Enchantments.CHANNELING),
		Map.entry("multishot", Enchantments.MULTISHOT),
		Map.entry("quick_charge", Enchantments.QUICK_CHARGE),
		Map.entry("piercing", Enchantments.PIERCING),
		Map.entry("density", Enchantments.DENSITY),
		Map.entry("breach", Enchantments.BREACH),
		Map.entry("wind_burst", Enchantments.WIND_BURST));

	public EnchantCommand()
	{
		super("enchant", "Enchants your held item client-side.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(argument("enchantment", StringArgumentType.word())
			.suggests((ctx, b) -> net.minecraft.commands.SharedSuggestionProvider
				.suggest(NAMES.keySet(), b))
			.executes(ctx -> enchant(
				ctx.getArgument("enchantment", String.class), 1))
			.then(argument("level", IntegerArgumentType.integer(1, 255))
				.executes(ctx -> enchant(
					ctx.getArgument("enchantment", String.class),
					IntegerArgumentType.getInteger(ctx, "level")))));
	}

	private int enchant(String name, int level)
	{
		ItemStack stack = mc().player.getMainHandItem();
		if(stack.isEmpty())
		{
			error("You are not holding an item.");
			return SINGLE_SUCCESS;
		}

		ResourceKey<Enchantment> key = NAMES.get(name.toLowerCase());
		if(key == null)
		{
			error("Unknown enchantment \"%s\".", name);
			return SINGLE_SUCCESS;
		}

		try
		{
			Holder<Enchantment> holder = mc().level.registryAccess()
				.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
			stack.enchant(holder, level);
			info("Enchanted held item with %s %d.", name, level);
		}catch(Exception e)
		{
			error("Could not apply enchantment: %s", e.getMessage());
		}

		return SINGLE_SUCCESS;
	}
}
