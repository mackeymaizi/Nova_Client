/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Camera;
import net.minecraft.world.level.material.FogType;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.CameraNoClipModule;
import net.novaclient.module.modules.render.NoOverlayModule;

@Mixin(Camera.class)
public abstract class CameraMixin
{
	@Inject(method = "getMaxZoom(F)F", at = @At("HEAD"), cancellable = true)
	private void nova_onGetMaxZoom(float desiredCameraDistance,
		CallbackInfoReturnable<Float> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(ModuleManager.get(CameraNoClipModule.class).isEnabled())
			cir.setReturnValue(desiredCameraDistance);
	}

	@Inject(
		method = "getFluidInCamera()Lnet/minecraft/world/level/material/FogType;",
		at = @At("HEAD"),
		cancellable = true)
	private void nova_onGetFluidInCamera(CallbackInfoReturnable<FogType> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(ModuleManager.get(NoOverlayModule.class).isEnabled())
			cir.setReturnValue(FogType.NONE);
	}
}
