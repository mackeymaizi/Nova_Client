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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.novaclient.NovaClient;
import net.novaclient.event.EventBus;
import net.novaclient.event.events.CameraBobEvent;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.AntiWobbleModule;
import net.novaclient.module.modules.render.FullbrightModule;
import net.novaclient.module.modules.render.NoHurtcamModule;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements AutoCloseable
{
	/** Prevents view bobbing when AntiWobble is enabled. */
	@WrapOperation(method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V",
			ordinal = 0))
	private void nova_onBobView(GameRenderer instance, PoseStack poseStack,
		float partialTick, Operation<Void> original)
	{
		CameraBobEvent event = CameraBobEvent.INSTANCE;
		event.setCancelled(false);
		EventBus.post(event);

		if(!event.isCancelled())
			original.call(instance, poseStack, partialTick);
	}

	/**
	 * Disables nausea and portal wobble when using AntiWobble, without the
	 * green tint that the vanilla setting creates.
	 *
	 * <p>1.21.11 reads {@code options.screenEffectScale} through an
	 * {@link OptionInstance}, so we wrap the read and substitute 0 when the
	 * module is on.
	 */
	@WrapOperation(method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
	private Object nova_onScreenEffectScale(OptionInstance<?> instance,
		Operation<Object> original)
	{
		Object value = original.call(instance);

		if(NovaClient.INSTANCE == null)
			return value;

		if(ModuleManager.get(AntiWobbleModule.class).isEnabled()
			&& instance == NovaClient.MC.options.screenEffectScale())
			return 0.0D;

		return value;
	}

	/** Makes NoHurtcam work. */
	@Inject(method = "bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V",
		at = @At("HEAD"),
		cancellable = true)
	private void nova_onBobHurt(PoseStack poseStack, float partialTick,
		CallbackInfo ci)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(ModuleManager.get(NoHurtcamModule.class).isEnabled())
			ci.cancel();
	}

	@Inject(
		method = "getNightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F",
		at = @At("HEAD"),
		cancellable = true)
	private static void nova_onNightVisionScale(LivingEntity entity,
		float tickDelta, CallbackInfoReturnable<Float> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		float strength =
			ModuleManager.get(FullbrightModule.class).getNightVisionStrength();

		if(strength > 0)
			cir.setReturnValue(strength);
	}
}
