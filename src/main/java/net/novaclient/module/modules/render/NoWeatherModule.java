/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.IntSetting;

/** Ported from Wurst's NoWeatherHack. */
public final class NoWeatherModule extends Module
{
	private final BoolSetting disableRain = new BoolSetting("Disable rain",
		"Stops rain and snow from rendering.", true);

	private final BoolSetting changeTime = new BoolSetting("Change time",
		"Forces the client-side time of day.", false);

	private final IntSetting time = new IntSetting("Time",
		"Time of day to force, in ticks. 6000 = noon, 18000 = midnight.", 6000,
		0, 23900, 100);

	private final BoolSetting changeMoonPhase = new BoolSetting(
		"Change moon phase", "Forces the moon phase.", false);

	private final IntSetting moonPhase = new IntSetting("Moon phase",
		"0 = full moon, 4 = new moon.", 0, 0, 7, 1);

	public NoWeatherModule()
	{
		super("NoWeather", "Removes rain and lets you change the time of day.",
			ModuleCategory.RENDER);
		displayName("无天气");

		addSetting(disableRain);
		addSetting(changeTime);
		addSetting(time);
		addSetting(changeMoonPhase);
		addSetting(moonPhase);
	}

	public boolean isRainDisabled()
	{
		return isEnabled() && disableRain.isChecked();
	}

	public boolean isTimeChanged()
	{
		return isEnabled() && changeTime.isChecked();
	}

	public long getChangedTime()
	{
		return time.get();
	}

	public boolean isMoonPhaseChanged()
	{
		return isEnabled() && changeMoonPhase.isChecked();
	}

	public int getChangedMoonPhase()
	{
		return moonPhase.get();
	}
}
