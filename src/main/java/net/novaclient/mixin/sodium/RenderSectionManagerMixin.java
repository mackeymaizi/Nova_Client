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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.client.Camera;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.XRayModule;

/**
 * Disables Sodium's occlusion culling while X-Ray is active.
 *
 * <p>Occlusion culling decides whether a chunk section is visible based on
 * block data, not on what was actually meshed. Since X-Ray removes blocks from
 * the mesh but leaves the block data untouched, Sodium would still cull every
 * chunk behind a wall - hiding exactly the ores X-Ray is meant to reveal.
 *
 * <h2>Why this hooks {@code shouldUseOcclusionCulling} and not setupTerrain</h2>
 *
 * <p>On Sodium <b>0.9.2</b> the first {@code boolean} of
 * {@code SodiumWorldRenderer.setupTerrain} is forwarded verbatim into
 * {@code RenderSectionManager.prepareRenderTrees -> scheduleAsyncWork ->
 * CullTask.useOcclusionCulling}, so forcing it to {@code false} really does
 * turn culling off. The 26.2 branch does exactly that.
 *
 * <p>Sodium <b>0.8.15</b> (the 1.21.11 line) inserted an extra step:
 *
 * <pre>
 * boolean shouldUseOcclusionCulling(Camera camera, boolean flag) {
 *     if (flag &amp;&amp; level.getBlockState(camera.blockPosition()).isSolidRender())
 *         return false;
 *     return Minecraft.getInstance().smartCull;   // defaults to true
 * }
 * </pre>
 *
 * <p>So passing {@code flag = false} only skips the "camera is inside a solid
 * block" special case - the {@code else} branch still returns
 * {@code smartCull}, which is {@code true} by default, and occlusion culling
 * stays on. The <i>return value</i> of this method is what
 * {@code createTerrainRenderList} hands to
 * {@code OcclusionCuller.findVisible(...)}, so that is what has to be forced.
 *
 * <p>There is no {@code LocalVariableTable} in the shipped Sodium jar, hence
 * the positional callback parameters rather than named ones.
 */
@Mixin(value = RenderSectionManager.class)
public abstract class RenderSectionManagerMixin
{
	@Inject(method = "shouldUseOcclusionCulling", at = @At("HEAD"),
		cancellable = true)
	private void nova_xrayOcclusionCulling(Camera camera, boolean flag,
		CallbackInfoReturnable<Boolean> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		XRayModule xray = ModuleManager.get(XRayModule.class);

		if(xray == null || !xray.isEnabled())
			return;

		cir.setReturnValue(false);
	}
}
