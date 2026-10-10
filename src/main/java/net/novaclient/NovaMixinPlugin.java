/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Decides which optional Mixin configs apply.
 *
 * <p>Right now this is only used for the Sodium integration: Sodium replaces
 * Minecraft's chunk rendering pipeline, so Nova's vanilla X-Ray mixin never
 * fires when it is installed. The {@code net.novaclient.mixin.sodium} package
 * holds the equivalent hooks for Sodium's renderer, and they are only applied
 * when Sodium is actually present.
 */
public final class NovaMixinPlugin implements IMixinConfigPlugin
{
	private static final String MIXIN_PACKAGE = "net.novaclient.mixin";
	private static final String SODIUM_PACKAGE = MIXIN_PACKAGE + ".sodium";

	private static boolean initialised;
	private static boolean sodiumPresent;

	@Override
	public void onLoad(String mixinPackage)
	{
		if(initialised)
			return;

		sodiumPresent = FabricLoader.getInstance().isModLoaded("sodium");
		initialised = true;
	}

	@Override
	public String getRefMapperConfig()
	{
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
	{
		if(mixinClassName.startsWith(SODIUM_PACKAGE))
			return sodiumPresent;

		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{}

	@Override
	public List<String> getMixins()
	{
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass,
		String mixinClassName, IMixinInfo mixinInfo)
	{}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass,
		String mixinClassName, IMixinInfo mixinInfo)
	{}

	/** True when Sodium is installed. Used by the X-Ray module. */
	public static boolean isSodiumPresent()
	{
		return sodiumPresent;
	}
}
