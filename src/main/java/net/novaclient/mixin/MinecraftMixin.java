/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.novaclient.NovaClient;
import net.novaclient.event.EventBus;
import net.novaclient.event.events.HandleInputEvent;
import net.novaclient.event.events.StartAttackEvent;
import net.novaclient.event.events.TickEvent;
import net.novaclient.mixininterface.IMinecraftClient;
import net.novaclient.mixininterface.IMultiPlayerGameMode;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.misc.NoTelemetryModule;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements IMinecraftClient
{
	@Shadow
	public MultiPlayerGameMode gameMode;
	@Shadow
	public LocalPlayer player;

	/**
	 * Runs just before {@link Minecraft#handleKeybinds()}, bypassing the
	 * {@code overlay == null && screen == null} check in tick().
	 *
	 * <p>26.2 anchored this on {@code Gui.overlay()}; on 1.21.11 the overlay is
	 * a field on {@link Minecraft} instead, so we hook the
	 * {@code handleKeybinds()} call itself.
	 */
	@Inject(method = "tick()V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/Minecraft;handleKeybinds()V",
			ordinal = 0))
	private void nova_onHandleKeybinds(CallbackInfo ci)
	{
		if(player == null)
			return;

		EventBus.post(HandleInputEvent.INSTANCE);
	}

	@Inject(method = "tick()V", at = @At("HEAD"))
	private void nova_onTickPre(CallbackInfo ci)
	{
		if(player == null)
			return;

		EventBus.post(new TickEvent(true));
	}

	@Inject(method = "tick()V", at = @At("RETURN"))
	private void nova_onTickPost(CallbackInfo ci)
	{
		if(player == null)
			return;

		EventBus.post(new TickEvent(false));
	}

	@Inject(method = "startAttack()Z",
		at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;",
			ordinal = 0),
		cancellable = true)
	private void nova_onStartAttack(CallbackInfoReturnable<Boolean> cir)
	{
		StartAttackEvent event = new StartAttackEvent();
		EventBus.post(event);

		if(event.isCancelled())
			cir.setReturnValue(false);
	}

	@Inject(method = "allowsTelemetry()Z", at = @At("HEAD"),
		cancellable = true)
	private void nova_onAllowsTelemetry(CallbackInfoReturnable<Boolean> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(ModuleManager.get(NoTelemetryModule.class).isEnabled())
			cir.setReturnValue(false);
	}

	@Inject(method = "extraTelemetryAvailable()Z", at = @At("HEAD"),
		cancellable = true)
	private void nova_onExtraTelemetryAvailable(
		CallbackInfoReturnable<Boolean> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(ModuleManager.get(NoTelemetryModule.class).isEnabled())
			cir.setReturnValue(false);
	}

	@Override
	public IMultiPlayerGameMode nova_getInteractionManager()
	{
		return (IMultiPlayerGameMode)gameMode;
	}
}
