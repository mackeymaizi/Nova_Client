/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.combat;

import java.util.Random;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.IntSetting;

/** Ported from Wurst's TriggerBotHack. Attacks whatever you look at. */
public final class TriggerBotModule extends Module
{
	private final DoubleSetting range = new DoubleSetting("Range",
		"Maximum attack range.", 4.25, 1, 6, 0.05);

	private final IntSetting delay = new IntSetting("Delay",
		"Ticks between attacks.", 1, 0, 20, 1);

	private final IntSetting randomization = new IntSetting(
		"Randomization", "Random extra delay in ticks, to look more human.",
		1, 0, 10, 1);

	private final BoolSetting swing = new BoolSetting("Swing hand",
		"Swings your hand when attacking.", true);

	private final BoolSetting ignorePassive = new BoolSetting("Ignore passive",
		"Skips passive mobs.", true);

	private final BoolSetting onlyOnClick = new BoolSetting("Only on click",
		"Only attacks while holding left click.", false);

	private final Random random = new Random();
	private int timer;

	public TriggerBotModule()
	{
		super("TriggerBot", "Automatically attacks the entity you look at.",
			ModuleCategory.COMBAT);
		displayName("自动攻击");

		addSetting(range);
		addSetting(delay);
		addSetting(randomization);
		addSetting(swing);
		addSetting(ignorePassive);
		addSetting(onlyOnClick);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player == null)
			return;

		if(onlyOnClick.isChecked()
			&& !NovaClient.MC.options.keyAttack.isDown())
			return;

		if(timer > 0)
		{
			timer--;
			return;
		}

		Entity target = NovaClient.MC.crosshairPickEntity;

		if(!(target instanceof LivingEntity) || target == player)
			return;

		if(target.distanceToSqr(player) > range.get() * range.get())
			return;

		if(ignorePassive.isChecked()
			&& target instanceof net.minecraft.world.entity.Mob mob
			&& !mob.isAggressive()
			&& !(target instanceof net.minecraft.world.entity.player.Player))
			return;

		NovaClient.MC.gameMode.attack(player, target);

		if(swing.isChecked())
			player.swing(InteractionHand.MAIN_HAND);

		timer = delay.get()
			+ (randomization.get() > 0 ? random.nextInt(randomization.get() + 1) : 0);
	}
}
