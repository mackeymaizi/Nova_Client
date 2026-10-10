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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.player.LocalPlayer;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.movement.NoSlowdownModule;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin
{
	private boolean nova_isNoSlowdown()
	{
		return NovaClient.INSTANCE != null
			&& ModuleManager.get(NoSlowdownModule.class).isEnabled();
	}

	/** Keeps you sprinting when using an item while NoSlowdown is enabled. */
	@WrapOperation(method = "aiStep()V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;isSlowDueToUsingItem()Z",
			ordinal = 0))
	private boolean nova_wrapAiStepItemUse(LocalPlayer instance,
		Operation<Boolean> original)
	{
		if(nova_isNoSlowdown())
			return false;

		return original.call(instance);
	}

	/** Prevents item-use movement slowdown while NoSlowdown is enabled. */
	@WrapOperation(
		method = "modifyInput(Lnet/minecraft/world/phys/Vec2;)Lnet/minecraft/world/phys/Vec2;",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
			ordinal = 0))
	private boolean nova_wrapModifyInputItemUse(LocalPlayer instance,
		Operation<Boolean> original)
	{
		if(nova_isNoSlowdown())
			return false;

		return original.call(instance);
	}

	/** Allows sprinting to start while using an item (NoSlowdown). */
	@WrapOperation(method = "canStartSprinting()Z",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;isSlowDueToUsingItem()Z",
			ordinal = 0))
	private boolean nova_wrapCanStartSprinting(LocalPlayer instance,
		Operation<Boolean> original)
	{
		if(nova_isNoSlowdown())
			return false;

		return original.call(instance);
	}
}
