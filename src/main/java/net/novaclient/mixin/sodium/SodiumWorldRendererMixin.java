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
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
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
 * <p>{@code setupTerrain(Camera, Viewport, FogParameters, boolean, boolean,
 * Matrix4f)} - the first boolean (local slot 4) is the occlusion culling flag,
 * confirmed by following it into {@code RenderSectionManager
 * .prepareRenderTrees(Viewport, FogParameters, boolean)}. The second boolean
 * (slot 5) drives {@code updateChunks} instead, so it must be left alone.
 *
 * <p>Sodium 0.9.2 ships without a LocalVariableTable, hence the slot index
 * rather than a parameter name.
 */
@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class SodiumWorldRendererMixin
{
	@ModifyVariable(method = "setupTerrain", at = @At("HEAD"),
		argsOnly = true, index = 4, remap = false)
	private boolean nova_xrayOcclusionCulling(boolean useOcclusionCulling)
	{
		if(NovaClient.INSTANCE == null)
			return useOcclusionCulling;

		XRayModule xray = ModuleManager.get(XRayModule.class);

		if(xray == null || !xray.isEnabled())
			return useOcclusionCulling;

		return false;
	}
}
