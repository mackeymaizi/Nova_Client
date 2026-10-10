/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.XRayModule;

/**
 * X-Ray support for Sodium's chunk renderer.
 *
 * <p>Sodium builds chunk meshes through its own pipeline, so the vanilla
 * {@code ModelBlockRenderer} hook never runs when Sodium is installed. This
 * mixin applies the same whitelist logic to Sodium's {@link BlockRenderer}.
 *
 * <p>Targets Sodium 0.8.15 for Minecraft 1.21.11. Sodium's own classes are not
 * in the mappings, so the annotation remapper leaves them alone while still
 * translating the Minecraft types to intermediary - which is why {@code remap}
 * is left at its default (true) here, unlike the 26.2 branch where Minecraft
 * is unobfuscated and nothing needs remapping at all.
 */
@Mixin(value = BlockRenderer.class)
public abstract class BlockRendererMixin
{
	@Shadow
	protected BlockState state;

	@Shadow
	protected BlockPos pos;

	@Shadow
	protected BlockAndTintGetter level;

	/**
	 * Hides every block that is not on X-Ray's whitelist. This is the
	 * "opacity = 0" path and covers the common case entirely.
	 */
	@Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
	private void nova_xrayRenderModel(BlockStateModel model,
		BlockState blockState, BlockPos blockPos, BlockPos origin,
		CallbackInfo ci)
	{
		if(isBlocked(blockState, blockPos, level))
			ci.cancel();
	}

	/**
	 * When an opacity above zero is configured, hidden blocks are drawn on the
	 * translucent layer instead of being removed.
	 *
	 * <p>The owner in the anchor is {@code MutableQuadViewImpl}, not
	 * {@code QuadViewImpl}: {@code getRenderType()} is declared on the parent
	 * but javac emits the receiver's static type, and Mixin matches the owner
	 * exactly. Targeting {@code QuadViewImpl} silently never matches.
	 */
	@ModifyExpressionValue(method = "processQuad",
		at = @At(value = "INVOKE",
			target = "Lnet/caffeinemc/mods/sodium/client/render/model/MutableQuadViewImpl;getRenderType()Lnet/minecraft/client/renderer/chunk/ChunkSectionLayer;"),
		require = 0)
	private ChunkSectionLayer nova_xrayRenderType(ChunkSectionLayer original)
	{
		return isDimmed(state, pos, level)
			? ChunkSectionLayer.TRANSLUCENT : original;
	}

	/** Applies X-Ray's alpha to the quad colour. */
	@ModifyExpressionValue(method = "bufferQuad",
		at = @At(value = "INVOKE",
			target = "Lnet/caffeinemc/mods/sodium/client/render/model/MutableQuadViewImpl;baseColor(I)I"),
		require = 0)
	private int nova_xrayBaseColor(int original)
	{
		if(!isDimmed(state, pos, level))
			return original;

		int alpha = getAlpha();
		return (original & 0x00FFFFFF) | (alpha << 24);
	}

	private static XRayModule xray()
	{
		if(NovaClient.INSTANCE == null)
			return null;

		return ModuleManager.get(XRayModule.class);
	}

	/** True when this block should not be rendered at all. */
	private static boolean isBlocked(BlockState state, BlockPos pos,
		BlockAndTintGetter level)
	{
		XRayModule xray = xray();

		if(xray == null || !xray.isEnabled())
			return false;

		if(!xray.isBlocked(state.getBlock()))
			return false;

		// Opacity mode keeps the block, it just fades it out.
		if(xray.getOpacity() > 0)
			return false;

		if(xray.isExposedOnly() && level != null && pos != null
			&& !isExposed(level, pos))
			return false;

		return true;
	}

	/** True when this block should be drawn with a reduced alpha. */
	private static boolean isDimmed(BlockState state, BlockPos pos,
		BlockAndTintGetter level)
	{
		XRayModule xray = xray();

		if(xray == null || !xray.isEnabled() || xray.getOpacity() <= 0)
			return false;

		if(!xray.isBlocked(state.getBlock()))
			return false;

		if(xray.isExposedOnly() && level != null && pos != null
			&& !isExposed(level, pos))
			return false;

		return true;
	}

	private static boolean isExposed(BlockAndTintGetter level, BlockPos pos)
	{
		for(Direction direction : Direction.values())
			if(level.getBlockState(pos.relative(direction)).isAir())
				return true;

		return false;
	}

	private static int getAlpha()
	{
		XRayModule xray = xray();
		return xray == null ? 255 : xray.getOpacity();
	}
}
