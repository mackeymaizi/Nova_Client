/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.noshieldoverlay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.NoShieldOverlayModule;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin
{
	/** Lowers the shield when blocking, if NoShieldOverlay is enabled. */
	@Inject(
		method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/ItemUseAnimation;",
			shift = At.Shift.AFTER))
	private void nova_onSubmitArmBlocking(AbstractClientPlayer player,
		float tickProgress, float pitch, InteractionHand hand,
		float swingProgress, ItemStack item, float equipProgress,
		PoseStack poseStack, SubmitNodeCollector nodeCollector, int light,
		CallbackInfo ci)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(item.getUseAnimation() != ItemUseAnimation.BLOCK)
			return;

		ModuleManager.get(NoShieldOverlayModule.class)
			.adjustShieldPosition(poseStack, true);
	}

	/** Lowers the shield when NOT blocking, if NoShieldOverlay is enabled. */
	@Inject(
		method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;getSwingAnimation()Lnet/minecraft/world/item/component/SwingAnimation;"))
	private void nova_onSubmitArmNotBlocking(AbstractClientPlayer player,
		float tickProgress, float pitch, InteractionHand hand,
		float swingProgress, ItemStack item, float equipProgress,
		PoseStack poseStack, SubmitNodeCollector nodeCollector, int light,
		CallbackInfo ci)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(item.getUseAnimation() != ItemUseAnimation.BLOCK)
			return;

		ModuleManager.get(NoShieldOverlayModule.class)
			.adjustShieldPosition(poseStack, false);
	}
}
