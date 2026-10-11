/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.misc;

import java.util.Random;

import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.IntSetting;

/**
 * Ported from Wurst's AntiAfkHack (simplified). Moves you around randomly so
 * that servers don't kick you for being idle.
 */
public final class AntiAfkModule extends Module
{
	private final BoolSetting walk = new BoolSetting("Walk",
		"Randomly walks around.", true);

	private final BoolSetting jump = new BoolSetting("Jump",
		"Occasionally jumps.", true);

	private final BoolSetting swing = new BoolSetting("Swing",
		"Swings your hand occasionally.", false);

	private final IntSetting interval = new IntSetting("Interval",
		"Seconds between actions.", 3, 1, 60, 1);

	private final IntSetting waitTimeRandom = new IntSetting("Wait time random",
		"Randomises the interval by up to this many seconds.", 0, 0, 30, 1);

	private final BoolSetting showWaitTime = new BoolSetting("Show wait time",
		"Shows the time until the next action next to the module name.", false);

	private final Random random = new Random();
	private int timer;
	private int moveTicks;
	private Action current = Action.None;

	public AntiAfkModule()
	{
		super("AntiAfk", "Prevents you from being kicked for being idle.",
			ModuleCategory.MISC);
		displayName("防挂机");

		addSetting(walk);
		addSetting(jump);
		addSetting(swing);
		addSetting(interval);
		addSetting(waitTimeRandom);
		addSetting(showWaitTime);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	@Override
	public String getInfoString()
	{
		if(!showWaitTime.isChecked())
			return null;

		return (timer / 20) + "s";
	}

	@Override
	public void onEnable()
	{
		timer = 0;
		moveTicks = 0;
		current = Action.None;
	}

	@Override
	public void onDisable()
	{
		release();
	}

	private void onUpdate()
	{
		var options = NovaClient.MC.options;

		if(moveTicks > 0)
		{
			moveTicks--;
			if(moveTicks == 0)
				release();
			return;
		}

		if(timer > 0)
		{
			timer--;
			return;
		}

		int base = interval.get();
		int jitter = waitTimeRandom.get() > 0
			? random.nextInt(waitTimeRandom.get() * 2 + 1) - waitTimeRandom.get()
			: 0;
		timer = Math.max(1, base + jitter) * 20;

		int choice = random.nextInt(3);

		if(choice == 0 && walk.isChecked())
		{
			current = Action.values()[1 + random.nextInt(4)];
			moveTicks = 10 + random.nextInt(20);
		}else if(choice == 1 && jump.isChecked())
			NovaClient.MC.options.keyJump.setDown(true);
		else if(swing.isChecked())
			NovaClient.MC.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);

		if(current != Action.None)
		{
			options.keyUp.setDown(current == Action.Forward);
			options.keyDown.setDown(current == Action.Backward);
			options.keyLeft.setDown(current == Action.Left);
			options.keyRight.setDown(current == Action.Right);
		}
	}

	private void release()
	{
		var options = NovaClient.MC.options;
		options.keyUp.setDown(false);
		options.keyDown.setDown(false);
		options.keyLeft.setDown(false);
		options.keyRight.setDown(false);
		options.keyJump.setDown(false);
		current = Action.None;
	}

	private enum Action
	{
		None, Forward, Backward, Left, Right;
	}
}
