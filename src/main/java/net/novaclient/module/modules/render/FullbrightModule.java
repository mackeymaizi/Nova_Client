/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.novaclient.NovaClient;
import net.novaclient.event.events.MobEffectEvent;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.EnumSetting;

/** Ported from Wurst's FullbrightHack. */
public final class FullbrightModule extends Module
{
	private final EnumSetting<Method> method = new EnumSetting<>("Method",
		"Gamma works by setting your brightness slider beyond 100%."
			+ " Incompatible with shader packs.\n\nNight Vision works by"
			+ " applying the night vision effect. This usually works with"
			+ " shader packs.",
		Method.values(), Method.GAMMA);

	private final BoolSetting fade = new BoolSetting("Fade",
		"Slowly fades between brightness and darkness.", true);

	private final DoubleSetting defaultGamma = new DoubleSetting(
		"Default brightness",
		"Fullbright will set your brightness slider back to this value"
			+ " when you turn it off.",
		0.5, 0, 1, 0.01);

	private final MobEffectInstance nightVisionInstance =
		new MobEffectInstance(MobEffects.NIGHT_VISION,
			MobEffectInstance.INFINITE_DURATION, 0, false, false);

	private boolean wasGammaChanged;
	private float nightVisionStrength;

	public FullbrightModule()
	{
		super("Fullbright", "Always renders the world at full brightness.",
			ModuleCategory.RENDER);
		displayName("夜视");

		addSetting(method);
		addSetting(fade);
		addSetting(defaultGamma);

		subscribe(UpdateEvent.class, e -> {
			updateGamma();
			updateNightVision();
		});

		subscribe(MobEffectEvent.class, e -> {
			if(e.effect() == MobEffects.NIGHT_VISION && nightVisionStrength > 0)
				e.setInstance(nightVisionInstance);
		});
	}

	@Override
	public void onDisable()
	{
		if(wasGammaChanged)
		{
			resetGamma(defaultGamma.get());
			wasGammaChanged = false;
		}

		nightVisionStrength = 0;
	}

	public float getNightVisionStrength()
	{
		return nightVisionStrength;
	}

	public boolean isChangingGamma()
	{
		return isEnabled() && method.get() == Method.GAMMA;
	}

	private void updateGamma()
	{
		if(isChangingGamma())
		{
			setGamma(16);
			return;
		}

		if(wasGammaChanged)
			resetGamma(defaultGamma.get());
	}

	private void setGamma(double target)
	{
		wasGammaChanged = true;

		double oldGamma = NovaClient.MC.options.gamma().get();
		if(!fade.isChecked() || Math.abs(oldGamma - target) <= 0.5)
		{
			NovaClient.MC.options.gamma().set(target);
			return;
		}

		NovaClient.MC.options.gamma()
			.set(oldGamma < target ? oldGamma + 0.5 : oldGamma - 0.5);
	}

	private void resetGamma(double target)
	{
		double oldGamma = NovaClient.MC.options.gamma().get();
		if(!fade.isChecked() || Math.abs(oldGamma - target) <= 0.5)
		{
			NovaClient.MC.options.gamma().set(target);
			wasGammaChanged = false;
			return;
		}

		NovaClient.MC.options.gamma()
			.set(oldGamma < target ? oldGamma + 0.5 : oldGamma - 0.5);
	}

	private void updateNightVision()
	{
		boolean shouldGiveNightVision =
			isEnabled() && method.get() == Method.NIGHT_VISION;

		if(fade.isChecked())
			nightVisionStrength = Mth.clamp(
				nightVisionStrength + (shouldGiveNightVision ? 0.03125F
					: -0.03125F),
				0, 1);
		else
			nightVisionStrength = shouldGiveNightVision ? 1 : 0;
	}

	public enum Method
	{
		GAMMA("Gamma"),
		NIGHT_VISION("Night Vision");

		private final String name;

		private Method(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
