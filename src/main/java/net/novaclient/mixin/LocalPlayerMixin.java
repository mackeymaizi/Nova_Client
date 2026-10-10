/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.EventBus;
import net.novaclient.event.events.IsPlayerInWaterEvent;
import net.novaclient.event.events.KnockbackEvent;
import net.novaclient.event.events.MobEffectEvent;
import net.novaclient.event.events.PostMotionEvent;
import net.novaclient.event.events.PreMotionEvent;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.mixininterface.ILocalPlayer;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.misc.PortalGuiModule;
import net.novaclient.module.modules.movement.HighJumpModule;
import net.novaclient.module.modules.movement.StepModule;
import net.novaclient.module.modules.player.AntiKnockbackModule;
import net.novaclient.module.modules.player.ReachModule;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer
	implements ILocalPlayer
{
	@Shadow
	@Final
	protected Minecraft minecraft;

	private Screen nova_tempScreen;

	private LocalPlayerMixin(ClientLevel level, GameProfile profile)
	{
		super(level, profile);
	}

	@Inject(method = "tick()V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V",
			ordinal = 0))
	private void nova_onTick(CallbackInfo ci)
	{
		EventBus.post(UpdateEvent.INSTANCE);
	}

	@Inject(method = "sendPosition()V", at = @At("HEAD"))
	private void nova_onSendPositionHead(CallbackInfo ci)
	{
		EventBus.post(PreMotionEvent.INSTANCE);
	}

	@Inject(method = "sendPosition()V", at = @At("TAIL"))
	private void nova_onSendPositionTail(CallbackInfo ci)
	{
		EventBus.post(PostMotionEvent.INSTANCE);
	}

	/**
	 * When PortalGUI is enabled, this temporarily sets the current screen to
	 * null so that handlePortalTransitionEffect() doesn't close it.
	 */
	@Inject(method = "handlePortalTransitionEffect(Z)V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;",
			ordinal = 0))
	private void nova_beforePortalTransition(boolean fromPortalEffect,
		CallbackInfo ci)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(!ModuleManager.get(PortalGuiModule.class).isEnabled())
			return;

		nova_tempScreen = minecraft.screen;
		minecraft.setScreen(null);
	}

	/** Restores the screen once handlePortalTransitionEffect() is done. */
	@Inject(method = "handlePortalTransitionEffect(Z)V",
		at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/player/LocalPlayer;portalEffectIntensity:F",
			opcode = Opcodes.GETFIELD,
			ordinal = 1))
	private void nova_afterPortalTransition(boolean fromPortalEffect,
		CallbackInfo ci)
	{
		if(nova_tempScreen == null)
			return;

		minecraft.setScreen(nova_tempScreen);
		nova_tempScreen = null;
	}

	@Override
	public void lerpMotion(Vec3 vec)
	{
		if(NovaClient.INSTANCE != null && (Object)this == NovaClient.MC.player)
			vec = ModuleManager.get(AntiKnockbackModule.class)
				.modifyKnockback(vec);

		super.lerpMotion(vec);
	}

	@Override
	public boolean isInWater()
	{
		boolean inWater = super.isInWater();
		if(NovaClient.INSTANCE == null)
			return inWater;

		IsPlayerInWaterEvent event = new IsPlayerInWaterEvent(inWater);
		EventBus.post(event);
		return event.inWater();
	}

	@Override
	public boolean nova_isInWaterBypass()
	{
		return super.isInWater();
	}

	@Override
	public boolean hasEffect(Holder<MobEffect> effect)
	{
		return getEffect(effect) != null;
	}

	@Override
	public MobEffectInstance getEffect(Holder<MobEffect> effect)
	{
		if(NovaClient.INSTANCE == null
			|| (Object)this != NovaClient.MC.player)
			return super.getEffect(effect);

		MobEffectEvent event =
			new MobEffectEvent(effect, super.getEffect(effect));
		EventBus.post(event);
		return event.instance();
	}

	@Override
	public double blockInteractionRange()
	{
		if(NovaClient.INSTANCE == null)
			return super.blockInteractionRange();

		ReachModule reach = ModuleManager.get(ReachModule.class);
		if(!reach.isEnabled())
			return super.blockInteractionRange();

		return reach.getReachDistance();
	}

	@Override
	public double entityInteractionRange()
	{
		if(NovaClient.INSTANCE == null)
			return super.entityInteractionRange();

		ReachModule reach = ModuleManager.get(ReachModule.class);
		if(!reach.isEnabled())
			return super.entityInteractionRange();

		return reach.getReachDistance();
	}

	/** Step - raises the step height while Simple mode is active. */
	@Override
	public float maxUpStep()
	{
		float original = super.maxUpStep();

		if(NovaClient.INSTANCE == null || (Object)this != NovaClient.MC.player)
			return original;

		return ModuleManager.get(StepModule.class).adjustStepHeight(original);
	}

	/** HighJump - adds extra jump power. */
	@Override
	protected float getJumpPower()
	{
		float original = super.getJumpPower();

		if(NovaClient.INSTANCE == null || (Object)this != NovaClient.MC.player)
			return original;

		return original
			+ ModuleManager.get(HighJumpModule.class).getAdditionalJumpMotion();
	}
}
