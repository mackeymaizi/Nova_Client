/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.noweather;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.Level;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.NoWeatherModule;

/** Makes NoWeather stop rain client-side. */
@Mixin(Level.class)
public abstract class LevelMixin
{
	@Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
	private void nova_isRaining(CallbackInfoReturnable<Boolean> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		NoWeatherModule noWeather = ModuleManager.get(NoWeatherModule.class);

		if(noWeather != null && noWeather.isRainDisabled())
			cir.setReturnValue(false);
	}

	@Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
	private void nova_getRainLevel(float partialTicks,
		CallbackInfoReturnable<Float> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		NoWeatherModule noWeather = ModuleManager.get(NoWeatherModule.class);

		if(noWeather != null && noWeather.isRainDisabled())
			cir.setReturnValue(0.0F);
	}
}
