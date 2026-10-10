/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.combat;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.novaclient.NovaClient;
import net.novaclient.event.events.TickEvent;
import net.novaclient.mixininterface.IMultiPlayerGameMode;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.EnumSetting;
import net.novaclient.setting.IntSetting;
import net.novaclient.util.EntityUtils;
import net.novaclient.util.InventoryUtils;

/**
 * Ported from Meteor's AutoTotem, enhanced with explosion prediction and a
 * pause-while-in-a-screen guard.
 */
public final class AutoTotemModule extends Module
{
	private final EnumSetting<Mode> mode = new EnumSetting<>("Mode",
		"Determines when to hold a totem; Strict will always hold.",
		Mode.values(), Mode.Smart);

	private final IntSetting delay = new IntSetting("Delay",
		"The ticks between slot movements.", 0, 0, 20, 1);

	private final IntSetting health = new IntSetting("Health",
		"The health to hold a totem at.", 10, 0, 36, 1);

	private final BoolSetting elytra = new BoolSetting("Elytra",
		"Always holds a totem when flying with an elytra.", true);

	private final BoolSetting fall = new BoolSetting("Fall",
		"Holds a totem when fall damage could kill you.", true);

	private final BoolSetting explosion = new BoolSetting("Explosion",
		"Holds a totem when a nearby crystal could kill you.", true);

	private final BoolSetting pauseInScreens = new BoolSetting(
		"Pause in screens", "Does not move items while a container is open.",
		true);

	public boolean locked;
	private int totems;
	private int ticks;

	public AutoTotemModule()
	{
		super("AutoTotem", "Automatically equips a totem in your offhand.",
			ModuleCategory.COMBAT);
		displayName("自动图腾");

		addSetting(mode);
		addSetting(delay);
		addSetting(health);
		addSetting(elytra);
		addSetting(fall);
		addSetting(explosion);
		addSetting(pauseInScreens);

		subscribe(TickEvent.class, this::onTick);
	}

	@Override
	public void onDisable()
	{
		locked = false;
	}

	private void onTick(TickEvent event)
	{
		if(!event.isPre())
			return;

		var mc = NovaClient.MC;
		var player = mc.player;

		if(player == null || mc.gameMode == null)
			return;

		totems = InventoryUtils.count(s -> s.is(Items.TOTEM_OF_UNDYING), 36,
			true);

		if(totems <= 0)
		{
			locked = false;
			return;
		}

		if(pauseInScreens.isChecked() && mc.screen != null)
			return;

		if(ticks < delay.get())
		{
			ticks++;
			return;
		}

		ticks = 0;

		float effectiveHealth = player.getHealth()
			+ player.getAbsorptionAmount();
		float possibleReduction = 0;

		if(fall.isChecked())
			possibleReduction += Math.max(0, player.fallDistance
				- player.getAttributeValue(
					net.minecraft.world.entity.ai.attributes.Attributes.SAFE_FALL_DISTANCE));

		if(explosion.isChecked())
			possibleReduction += nearbyCrystalDamage(player);

		boolean low = effectiveHealth - possibleReduction <= health.get();
		boolean ely = elytra.isChecked()
			&& player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)
			&& player.isFallFlying();

		locked = mode.get() == Mode.Strict
			|| (mode.get() == Mode.Smart && (low || ely));

		if(locked && !player.getOffhandItem().is(Items.TOTEM_OF_UNDYING))
		{
			int slot = InventoryUtils.indexOf(
				s -> s.is(Items.TOTEM_OF_UNDYING), 36);

			if(slot != -1)
				moveToOffhand(slot);
		}
	}

	/**
	 * Very rough estimate of the damage a nearby end crystal could deal,
	 * based on distance falloff (max 12 at point blank).
	 */
	private float nearbyCrystalDamage(Player player)
	{
		float worst = 0;

		for(EndCrystal crystal : EntityUtils.getAliveEntities(EndCrystal.class))
		{
			double distance = Math.sqrt(crystal.distanceToSqr(player));
			if(distance > 6)
				continue;

			float damage = (float)Math.max(0, 12 * (1 - distance / 6));
			worst = Math.max(worst, damage);
		}

		return worst;
	}

	private void moveToOffhand(int slot)
	{
		IMultiPlayerGameMode gameMode =
			(IMultiPlayerGameMode)NovaClient.MC.gameMode;

		int networkSlot = InventoryUtils.toNetworkSlot(slot);

		// Pick the item up onto the cursor, then drop it into the offhand.
		gameMode.nova_windowClick_PICKUP(networkSlot);
		gameMode.nova_windowClick_PICKUP(InventoryUtils.OFFHAND_PKT_ID);

		// Return whatever is left on the cursor to its original slot.
		ItemStack carried = NovaClient.MC.player.containerMenu.getCarried();
		if(!carried.isEmpty())
			gameMode.nova_windowClick_PICKUP(networkSlot);
	}

	public boolean isLocked()
	{
		return isEnabled() && locked;
	}

	@Override
	public String getInfoString()
	{
		return totems + (locked ? " \u2714" : "");
	}

	public enum Mode
	{
		Smart, Strict;
	}
}
