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

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.novaclient.NovaClient;
import net.novaclient.event.EventBus;
import net.novaclient.event.events.Render2DEvent;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.NoOverlayModule;
import net.novaclient.module.modules.render.NoPumpkinModule;
import net.novaclient.module.modules.render.NoVignetteModule;

@Mixin(Hud.class)
public class HudMixin
{
	@Inject(
		method = "extractTabList(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
		at = @At("HEAD"))
	private void nova_onExtractTabList(GuiGraphicsExtractor context,
		DeltaTracker deltaTracker, CallbackInfo ci)
	{
		if(NovaClient.MC.debugEntries.isOverlayVisible())
			return;

		float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
		EventBus.post(new Render2DEvent(context, tickDelta));
	}

	@Inject(
		method = "extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V",
		at = @At("HEAD"),
		cancellable = true)
	private void nova_onExtractTextureOverlay(GuiGraphicsExtractor context,
		Identifier texture, float opacity, CallbackInfo ci)
	{
		if(texture == null || NovaClient.INSTANCE == null)
			return;

		String path = texture.getPath();

		if("textures/misc/pumpkinblur.png".equals(path)
			&& ModuleManager.get(NoPumpkinModule.class).isEnabled())
			ci.cancel();

		if("textures/misc/powder_snow_outline.png".equals(path)
			&& ModuleManager.get(NoOverlayModule.class).isEnabled())
			ci.cancel();
	}

	@Inject(method = "extractVignette", at = @At("HEAD"), cancellable = true)
	private void nova_onExtractVignette(GuiGraphicsExtractor context,
		Entity entity, CallbackInfo ci)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(ModuleManager.get(NoVignetteModule.class).isEnabled())
			ci.cancel();
	}
}
