/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.nofog;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.NoFogModule;

@Mixin(FogRenderer.class)
public class FogRendererMixin
{
	/**
	 * Removes the fog near the end of the render distance in all dimensions,
	 * if NoFog is enabled.
	 */
	@Inject(
		method = "setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lorg/joml/Vector4f;",
		at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F",
			opcode = Opcodes.PUTFIELD,
			shift = At.Shift.AFTER))
	private void nova_modifyFogData(Camera camera, int renderDistanceInChunks,
		DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level,
		CallbackInfoReturnable<org.joml.Vector4f> cir, @Local FogData fog)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(!ModuleManager.get(NoFogModule.class).isEnabled())
			return;

		fog.renderDistanceStart = 1000000;
		fog.renderDistanceEnd = 1000000;
	}
}
