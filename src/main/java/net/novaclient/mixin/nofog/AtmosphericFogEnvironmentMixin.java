/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.nofog;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.NoFogModule;

@Mixin(AtmosphericFogEnvironment.class)
public class AtmosphericFogEnvironmentMixin
{
	/**
	 * Removes the foggy overlay in the Overworld (including rain fog), if
	 * NoFog is enabled.
	 */
	@Inject(method = "setupFog", at = @At("TAIL"), cancellable = true)
	private void nova_onSetupFog(FogData data, Camera camera, ClientLevel level,
		float viewDistance, DeltaTracker deltaTracker, CallbackInfo ci)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(!ModuleManager.get(NoFogModule.class).isEnabled())
			return;

		data.environmentalStart = 1000000;
		data.environmentalEnd = 1000000;
	}
}
