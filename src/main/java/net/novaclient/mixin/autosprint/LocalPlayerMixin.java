/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.autosprint;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.movement.AutoSprintModule;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin
{
	/** Makes AutoSprint's "Omnidirectional Sprint" setting work. */
	@WrapOperation(method = "shouldStopRunSprinting()Z",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/player/ClientInput;hasForwardImpulse()Z"))
	private boolean nova_wrapHasForwardImpulse(ClientInput input,
		Operation<Boolean> original)
	{
		if(NovaClient.INSTANCE != null
			&& ModuleManager.get(AutoSprintModule.class).shouldOmniSprint())
			return input.getMoveVector().length() > 1e-5F;

		return original.call(input);
	}

	/** Allows AutoSprint to sprint even when the player is too hungry. */
	@Inject(method = "isSprintingPossible(Z)Z", at = @At("HEAD"),
		cancellable = true)
	private void nova_onIsSprintingPossible(boolean allowTouchingWater,
		CallbackInfoReturnable<Boolean> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(ModuleManager.get(AutoSprintModule.class).shouldSprintHungry())
			cir.setReturnValue(true);
	}
}
