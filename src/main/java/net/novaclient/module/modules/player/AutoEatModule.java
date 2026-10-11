/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.player;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.IntSetting;

/** Ported from Wurst's AutoEatHack. Eats when you get hungry. */
public final class AutoEatModule extends Module
{
	private final IntSetting hunger = new IntSetting("Hunger",
		"Eat when your hunger drops to this level.", 17, 1, 20, 1);

	private final IntSetting health = new IntSetting("Health",
		"Also eat when your health drops to this level.", 10, 1, 36, 1);

	private final BoolSetting preferGaps = new BoolSetting("Prefer gaps",
		"Prefer golden apples over regular food.", true);

	private final BoolSetting offhand = new BoolSetting("Offhand",
		"Also eat from your offhand.", false);

	private final BoolSetting eatWhileWalking = new BoolSetting("Eat while walking",
		"Keep eating even while you are moving.", false);

	private final BoolSetting allowHunger = new BoolSetting("Allow hunger effect",
		"Allow eating food that gives you the Hunger effect.", true);

	private final BoolSetting allowPoison = new BoolSetting("Allow poison effect",
		"Allow eating food that gives you the Poison effect.", false);

	private final BoolSetting allowChorus = new BoolSetting("Allow chorus fruit",
		"Allow eating chorus fruit.", false);

	private int oldSlot = -1;

	public AutoEatModule()
	{
		super("AutoEat", "Automatically eats food when you get hungry.",
			ModuleCategory.PLAYER);
		displayName("自动进食");

		addSetting(hunger);
		addSetting(health);
		addSetting(preferGaps);
		addSetting(offhand);
		addSetting(eatWhileWalking);
		addSetting(allowHunger);
		addSetting(allowPoison);
		addSetting(allowChorus);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	@Override
	public void onDisable()
	{
		stopEating();
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player == null)
			return;

		FoodData foodData = player.getFoodData();

		boolean needsFood = foodData.getFoodLevel() <= hunger.get()
			|| player.getHealth() <= health.get();

		// "Eat while walking" off = only eat once you have stopped moving.
		if(!eatWhileWalking.isChecked() && player.input.getMoveVector().lengthSquared()
			> 1.0E-5F)
			needsFood = false;

		if(!needsFood || player.isUsingItem())
		{
			if(!player.isUsingItem())
				stopEating();
			return;
		}

		if(player.isUsingItem())
			return;

		int slot = findFood();

		if(slot == -1)
		{
			stopEating();
			return;
		}

		if(oldSlot == -1)
			oldSlot = player.getInventory().getSelectedSlot();

		player.getInventory().setSelectedSlot(slot);
		NovaClient.MC.gameMode.useItem(player, InteractionHand.MAIN_HAND);
	}

	private void stopEating()
	{
		var player = NovaClient.MC.player;

		if(player != null && oldSlot != -1)
		{
			player.getInventory().setSelectedSlot(oldSlot);
			oldSlot = -1;
		}
	}

	private int findFood()
	{
		var inventory = NovaClient.MC.player.getInventory();
		int fallback = -1;

		for(int i = 0; i < 9; i++)
		{
			ItemStack stack = inventory.getItem(i);

			if(stack.isEmpty() || stack.get(DataComponents.FOOD) == null)
				continue;

			if(!isAllowed(stack))
				continue;

			if(preferGaps.isChecked() && stack.is(net.minecraft.world.item.Items.GOLDEN_APPLE))
				return i;

			if(fallback == -1)
				fallback = i;
		}

		return fallback;
	}

	/** Applies the Wurst-style food filters (chorus / hunger / poison). */
	private boolean isAllowed(ItemStack stack)
	{
		var consumable = stack.get(DataComponents.CONSUMABLE);

		if(consumable == null)
			return true;

		for(var effect : consumable.onConsumeEffects())
		{
			if(effect instanceof net.minecraft.world.item.consume_effects
				.TeleportRandomlyConsumeEffect && !allowChorus.isChecked())
				return false;

			if(effect instanceof net.minecraft.world.item.consume_effects
				.ApplyStatusEffectsConsumeEffect apply)
				for(var instance : apply.effects())
				{
					var holder = instance.getEffect();

					if(!allowPoison.isChecked()
						&& holder.is(net.minecraft.world.effect.MobEffects.POISON))
						return false;

					if(!allowHunger.isChecked()
						&& holder.is(net.minecraft.world.effect.MobEffects.HUNGER))
						return false;
				}
		}

		return true;
	}
}
