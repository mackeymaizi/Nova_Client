/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.snowshoe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.movement.SnowShoeModule;

@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowBlockMixin extends Block
	implements BucketPickup
{
	private PowderSnowBlockMixin(Properties settings)
	{
		super(settings);
	}

	@Inject(
		method = "canEntityWalkOnPowderSnow(Lnet/minecraft/world/entity/Entity;)Z",
		at = @At("HEAD"),
		cancellable = true)
	private static void nova_onCanEntityWalkOnPowderSnow(Entity entity,
		CallbackInfoReturnable<Boolean> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(!ModuleManager.get(SnowShoeModule.class).isEnabled())
			return;

		if(entity == NovaClient.MC.player)
			cir.setReturnValue(true);
	}
}
