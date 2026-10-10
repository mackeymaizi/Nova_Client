/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.noslowdown;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.movement.NoSlowdownModule;

@Mixin(Block.class)
public abstract class BlockMixin implements ItemLike
{
	@Inject(method = "getSpeedFactor()F", at = @At("HEAD"),
		cancellable = true)
	private void nova_onGetSpeedFactor(CallbackInfoReturnable<Float> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(!ModuleManager.get(NoSlowdownModule.class).isEnabled())
			return;

		if(cir.getReturnValueF() < 1)
			cir.setReturnValue(1F);
	}
}
