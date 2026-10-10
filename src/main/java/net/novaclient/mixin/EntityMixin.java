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

import net.minecraft.commands.CommandSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityAccess;
import net.novaclient.NovaClient;
import net.novaclient.event.EventBus;
import net.novaclient.event.events.EntityPushEvent;
import net.novaclient.event.events.FluidPushEvent;
import net.novaclient.event.events.InvisibleToEvent;

@Mixin(Entity.class)
public abstract class EntityMixin
	implements Nameable, EntityAccess, CommandSource
{
	/**
	 * Prevents fluid currents from pushing the local player while
	 * AntiWaterPush is enabled.
	 */
	@WrapOperation(
		method = "updateFluidHeightAndDoFluidPushing(Lnet/minecraft/tags/TagKey;D)Z",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;isPushedByFluid()Z",
			ordinal = 0))
	private boolean nova_wrapIsPushedByFluid(Entity instance,
		Operation<Boolean> original)
	{
		if(NovaClient.INSTANCE == null || instance != NovaClient.MC.player)
			return original.call(instance);

		FluidPushEvent event = new FluidPushEvent();
		EventBus.post(event);

		if(!event.pushedByFluid())
			return false;

		return original.call(instance);
	}

	/** Fires {@link EntityPushEvent} when something tries to push the player. */
	@Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V",
		at = @At("HEAD"),
		cancellable = true)
	private void nova_onPush(Entity entity, CallbackInfo ci)
	{
		if(NovaClient.INSTANCE == null || (Object)this != NovaClient.MC.player)
			return;

		EntityPushEvent event = new EntityPushEvent(entity);
		EventBus.post(event);

		if(event.isCancelled())
			ci.cancel();
	}

	/** Makes invisible entities render as ghosts if TrueSight is enabled. */
	@Inject(
		method = "isInvisibleTo(Lnet/minecraft/world/entity/player/Player;)Z",
		at = @At("RETURN"),
		cancellable = true)
	private void nova_onIsInvisibleTo(Player player,
		CallbackInfoReturnable<Boolean> cir)
	{
		if(!cir.getReturnValueZ() || NovaClient.INSTANCE == null)
			return;

		InvisibleToEvent event =
			new InvisibleToEvent((Entity)(Object)this, true);
		EventBus.post(event);

		if(!event.invisible())
			cir.setReturnValue(false);
	}
}
