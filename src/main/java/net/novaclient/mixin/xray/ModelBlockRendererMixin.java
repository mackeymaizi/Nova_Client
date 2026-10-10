/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.xray;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.XRayModule;

/**
 * Hides every block that is not on X-Ray's whitelist by cancelling its
 * tessellation.
 *
 * <p>Note: 1.21.11's {@code tesselateBlock} takes the block model parts instead
 * of a {@code BlockStateModel}.
 */
@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin
{
	@Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
	private void nova_xrayTesselate(BlockAndTintGetter level,
		List<BlockModelPart> parts, BlockState state, BlockPos pos,
		PoseStack poseStack, VertexConsumer consumer, boolean checkSides,
		int seed, CallbackInfo ci)
	{
		if(NovaClient.INSTANCE == null)
			return;

		XRayModule xray = ModuleManager.get(XRayModule.class);

		if(xray == null || !xray.isEnabled())
			return;

		if(!xray.isBlocked(state.getBlock()))
			return;

		if(xray.isExposedOnly() && !isExposed(level, pos))
			return;

		ci.cancel();
	}

	private static boolean isExposed(BlockAndTintGetter level, BlockPos pos)
	{
		for(net.minecraft.core.Direction direction : net.minecraft.core.Direction
			.values())
			if(level.getBlockState(pos.relative(direction)).isAir())
				return true;

		return false;
	}
}
