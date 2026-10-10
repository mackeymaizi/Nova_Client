/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.nametags;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.entity.MobRenderer;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.NameTagsModule;

@Mixin(MobRenderer.class)
public abstract class MobRendererMixin
{
	/** Makes name-tagged mobs always show their nametags (NameTags). */
	@Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Mob;D)Z",
		at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;crosshairPickEntity:Lnet/minecraft/world/entity/Entity;",
			opcode = Opcodes.GETFIELD,
			ordinal = 0),
		cancellable = true)
	private void nova_onShouldShowName(CallbackInfoReturnable<Boolean> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(ModuleManager.get(NameTagsModule.class).shouldForceMobNametags())
			cir.setReturnValue(true);
	}
}
