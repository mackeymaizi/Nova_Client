/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.player;

import java.util.List;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.IntSetting;

/**
 * Ported from Wurst's AutoPotionHack. Drinks a potion when the configured
 * effect is missing or about to expire.
 */
public final class AutoPotionModule extends Module
{
	private final BoolSetting regen = new BoolSetting("Regeneration",
		"Drink a potion of Regeneration when needed.", true);

	private final BoolSetting fireResistance = new BoolSetting(
		"Fire Resistance", "Drink a potion of Fire Resistance when needed.",
		true);

	private final BoolSetting strength = new BoolSetting("Strength",
		"Drink a potion of Strength when needed.", false);

	private final BoolSetting swiftness = new BoolSetting("Swiftness",
		"Drink a potion of Swiftness when needed.", false);

	private final IntSetting minDuration = new IntSetting("Min duration",
		"Only re-drink when the effect has less than this many seconds left.",
		10, 0, 120, 1);

	private final IntSetting health = new IntSetting("Health",
		"Drink a Regeneration potion when your health drops to this level.",
		16, 1, 36, 1);

	private int oldSlot = -1;
	private int timer;

	public AutoPotionModule()
	{
		super("AutoPotion", "Automatically drinks potions when needed.",
			ModuleCategory.PLAYER);
		displayName("自动喝药");

		addSetting(regen);
		addSetting(fireResistance);
		addSetting(strength);
		addSetting(swiftness);
		addSetting(minDuration);
		addSetting(health);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	@Override
	public void onDisable()
	{
		restoreSlot();
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player == null || player.isUsingItem())
			return;

		if(timer > 0)
		{
			timer--;
			return;
		}

		int slot = findPotion();
		if(slot == -1)
		{
			restoreSlot();
			return;
		}

		if(oldSlot == -1)
			oldSlot = player.getInventory().getSelectedSlot();

		player.getInventory().setSelectedSlot(slot);
		NovaClient.MC.gameMode.useItem(player, InteractionHand.MAIN_HAND);
		timer = 40;
	}

	private void restoreSlot()
	{
		var player = NovaClient.MC.player;

		if(player != null && oldSlot != -1)
		{
			player.getInventory().setSelectedSlot(oldSlot);
			oldSlot = -1;
		}
	}

	private int findPotion()
	{
		var player = NovaClient.MC.player;
		var inventory = player.getInventory();

		for(int i = 0; i < 9; i++)
		{
			ItemStack stack = inventory.getItem(i);

			if(stack.isEmpty() || !stack.is(Items.POTION))
				continue;

			PotionContents contents = stack
				.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);

			if(contents == null)
				continue;

			if(hasNeededEffect(player, contents))
				return i;
		}

		return -1;
	}

	private boolean hasNeededEffect(net.minecraft.world.entity.player.Player player,
		PotionContents contents)
	{
		java.util.List<MobEffectInstance> effects = new java.util.ArrayList<>();
		contents.getAllEffects().forEach(effects::add);

		for(MobEffectInstance effect : effects)
		{
			var type = effect.getEffect();

			if(type.is(MobEffects.REGENERATION) && regen.isChecked())
				return needsEffect(player, type);
			if(type.is(MobEffects.FIRE_RESISTANCE) && fireResistance.isChecked())
				return needsEffect(player, type);
			if(type.is(MobEffects.STRENGTH) && strength.isChecked())
				return needsEffect(player, type);
			if(type.is(MobEffects.SPEED) && swiftness.isChecked())
				return needsEffect(player, type);
		}

		return false;
	}

	private boolean needsEffect(net.minecraft.world.entity.player.Player player,
		net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect)
	{
		MobEffectInstance current = player.getEffect(effect);

		if(current == null)
			return true;

		return current.getDuration() < minDuration.get() * 20;
	}
}
