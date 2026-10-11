/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * <h2>Why there are no {@code @Shadow} fields</h2>
 *
 * <p>The block currently being meshed lives in fields declared on
 * {@code AbstractBlockRenderContext} - the <em>superclass</em> of
 * {@code BlockRenderer} - not on {@code BlockRenderer} itself. Mixin can only
 * resolve an inherited {@code @Shadow} through a refMap, and this project has
 * none, so {@code @Shadow state} fails at apply time with <em>"@Shadow field
 * state was not located in the target class"</em> and the whole mixin is
 * dropped, silently disabling X-Ray whenever Sodium is installed.
 *
 * <p>Instead the values are taken from method parameters, which are always
 * there: {@code prepare} receives the {@link LevelSlice} and {@code renderModel}
 * receives the {@link BlockState} and {@link BlockPos}. Both run on the same
 * chunk-build worker thread for a given section, so a {@link ThreadLocal}
 * carries them across to the quad-level hooks below.
 *
 * <p>Targets Sodium 0.9.2 for Minecraft 26.2. Sodium ships unobfuscated, hence
 * {@code remap = false} on every injector.
 */
@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin
{
	/** Block + level currently being meshed by this worker thread. */
	private static final ThreadLocal<Ctx> nova$ctx = new ThreadLocal<>();

	private static final class Ctx
	{
		BlockState state;
		BlockPos pos;
		BlockAndTintGetter level;
	}

	/** The level is handed to Sodium once per chunk build. */
	@Inject(method = "prepare", at = @At("HEAD"), remap = false)
	private void nova$xrayPrepare(ChunkBuildBuffers buffers, LevelSlice level,
		TranslucentGeometryCollector collector, CallbackInfo ci)
	{
		Ctx ctx = nova$ctx.get();

		if(ctx == null)
		{
			ctx = new Ctx();
			nova$ctx.set(ctx);
		}

		ctx.level = level;
	}

	/** Called for every block; this is the hook that actually hides it. */
	@Inject(method = "renderModel", at = @At("HEAD"), cancellable = true,
		remap = false)
	private void nova$xrayRenderModel(BlockStateModel model,
		BlockState blockState, BlockPos blockPos, BlockPos origin,
		CallbackInfo ci)
	{
		Ctx ctx = nova$ctx.get();

		if(ctx == null)
		{
			ctx = new Ctx();
			nova$ctx.set(ctx);
		}

		ctx.state = blockState;
		ctx.pos = blockPos;

		if(isBlocked(blockState, blockPos, ctx.level))
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
		require = 0, remap = false)
	private ChunkSectionLayer nova$xrayRenderType(ChunkSectionLayer original)
	{
		Ctx ctx = nova$ctx.get();

		if(ctx == null || !isDimmed(ctx.state, ctx.pos, ctx.level))
			return original;

		return ChunkSectionLayer.TRANSLUCENT;
	}

	/** Applies X-Ray's alpha to the quad colour. */
	@ModifyExpressionValue(method = "bufferQuad",
		at = @At(value = "INVOKE",
			target = "Lnet/caffeinemc/mods/sodium/client/render/model/MutableQuadViewImpl;baseColor(I)I"),
		require = 0, remap = false)
	private int nova$xrayBaseColor(int original)
	{
		Ctx ctx = nova$ctx.get();

		if(ctx == null || !isDimmed(ctx.state, ctx.pos, ctx.level))
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

		if(xray == null || state == null || !xray.isEnabled())
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

		if(xray == null || state == null || !xray.isEnabled()
			|| xray.getOpacity() <= 0)
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
