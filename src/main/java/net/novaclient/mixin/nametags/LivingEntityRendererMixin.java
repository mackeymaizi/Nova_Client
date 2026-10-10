/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.nametags;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.NameTagsModule;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin
{
	/** Forces the nametag to be rendered if configured in NameTags. */
	@Inject(
		method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/Minecraft;getInstance()Lnet/minecraft/client/Minecraft;",
			ordinal = 0),
		cancellable = true)
	private void nova_shouldForceLabel(LivingEntity entity, double distanceSq,
		CallbackInfoReturnable<Boolean> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(ModuleManager.get(NameTagsModule.class)
			.shouldForcePlayerNametags())
			cir.setReturnValue(true);
	}
}
