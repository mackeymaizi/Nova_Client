/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.TickEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.IntSetting;

/**
 * Ported from Meteor Rejects' Boost. Works like a dash move: toggling it
 * throws you forwards once, or continuously with Auto boost on.
 */
public final class BoostModule extends Module
{
	private final DoubleSetting strength = new DoubleSetting("Strength",
		"Strength to yeet you with.", 4.0, 0, 10, 0.5);

	private final BoolSetting autoBoost = new BoolSetting("Auto boost",
		"Automatically boosts you.", false);

	private final IntSetting interval = new IntSetting("Interval",
		"Boost interval in ticks.", 20, 1, 120, 1);

	private int timer;

	public BoostModule()
	{
		super("Boost", "Works like a dash move.", ModuleCategory.MOVEMENT);
		displayName("冲刺");

		addSetting(strength);
		addSetting(autoBoost);
		addSetting(interval);

		subscribe(TickEvent.class, this::onTick);
	}

	@Override
	public void onEnable()
	{
		timer = interval.get();

		// Manual mode: one shot, then turn straight back off.
		if(!autoBoost.isChecked())
		{
			if(NovaClient.MC.player != null)
				boost();

			setEnabled(false);
		}
	}

	@Override
	public String getInfoString()
	{
		return String.format("%.1f", strength.get());
	}

	private void onTick(TickEvent event)
	{
		if(!event.isPre() || !autoBoost.isChecked())
			return;

		if(timer < 1)
		{
			boost();
			timer = interval.get();
		}else
			timer--;
	}

	private void boost()
	{
		Vec3 forward = NovaClient.MC.player.getForward().scale(strength.get());
		NovaClient.MC.player.push(forward.x, forward.y, forward.z);
	}
}
