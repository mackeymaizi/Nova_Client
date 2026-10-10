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

import net.minecraft.client.telemetry.ClientTelemetryManager;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.misc.NoTelemetryModule;

@Mixin(ClientTelemetryManager.class)
public class ClientTelemetryManagerMixin
{
	@Inject(
		method = "getOutsideSessionSender()Lnet/minecraft/client/telemetry/TelemetryEventSender;",
		at = @At("HEAD"),
		cancellable = true)
	private void nova_onGetOutsideSessionSender(
		CallbackInfoReturnable<TelemetryEventSender> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(ModuleManager.get(NoTelemetryModule.class).isEnabled())
			cir.setReturnValue(TelemetryEventSender.DISABLED);
	}
}
