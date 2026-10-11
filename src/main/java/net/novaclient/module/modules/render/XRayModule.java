/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.IntSetting;
import net.novaclient.util.SodiumCompat;

/**
 * Ported from Meteor's Xray. Hides every block that is not on the whitelist.
 *
 * <p>Note: this is the <em>hide</em> half of the pair - see
 * {@link SearchModule} for highlighting blocks instead.
 *
 * <p>Works with and without Sodium. Sodium replaces Minecraft's chunk renderer
 * entirely, so the vanilla hook never fires when it is installed; the
 * {@code net.novaclient.mixin.sodium} package supplies the equivalent hooks
 * and is only applied when Sodium is actually loaded.
 */
public final class XRayModule extends Module
{
	/** Default whitelist - the usual ores plus a few useful blocks. */
	private static final String[] DEFAULT_BLOCKS = {
		"coal_ore", "deepslate_coal_ore", "iron_ore", "deepslate_iron_ore",
		"copper_ore", "deepslate_copper_ore", "gold_ore", "deepslate_gold_ore",
		"redstone_ore", "deepslate_redstone_ore", "lapis_ore",
		"deepslate_lapis_ore", "diamond_ore", "deepslate_diamond_ore",
		"emerald_ore", "deepslate_emerald_ore", "nether_gold_ore",
		"nether_quartz_ore", "ancient_debris", "amethyst_cluster",
		"budding_amethyst", "chest", "trapped_chest", "ender_chest",
		"spawner", "trial_spawner", "vault", "crafting_table", "furnace",
		"anvil", "enchanting_table", "brewing_stand", "beacon", "ladder",
		"torch", "wall_torch", "glowstone", "water", "lava", "tnt",
		"sculk_catalyst", "sculk_sensor", "sculk_shrieker", "bone_block",
		"raw_iron_block", "raw_gold_block", "raw_copper_block", "iron_block",
		"gold_block", "diamond_block", "emerald_block", "lapis_block",
		"redstone_block", "coal_block", "command_block",
		"chain_command_block", "repeating_command_block", "end_portal_frame"
	};

	private final BoolSetting exposedOnly = new BoolSetting(
		"Only exposed", "Only show whitelisted blocks that touch air.",
		false);

	private final IntSetting opacity = new IntSetting("Opacity",
		"How opaque non-whitelisted blocks are. 0 = invisible.", 0, 0, 254, 5);

	private final IntSetting fluidOpacity = new IntSetting("Fluid opacity",
		"How opaque non-whitelisted fluids are. 0 = invisible.", 0, 0, 254, 5);

	private final Set<Block> whitelist = ConcurrentHashMap.newKeySet();

	public XRayModule()
	{
		super("XRay", "Only renders blocks you care about. Good for mining.",
			ModuleCategory.RENDER);
		displayName("X光");

		addSetting(exposedOnly);
		addSetting(opacity);
		addSetting(fluidOpacity);

		loadDefaults();
	}

	private void loadDefaults()
	{
		for(String name : DEFAULT_BLOCKS)
		{
			Block block = BuiltInRegistries.BLOCK
				.getValue(Identifier.withDefaultNamespace(name));

			if(block != null && block != Blocks.AIR)
				whitelist.add(block);
		}
	}

	/** True when the block should be hidden by X-Ray. */
	public boolean isBlocked(Block block)
	{
		return isEnabled() && !whitelist.contains(block);
	}

	public boolean isExposedOnly()
	{
		return exposedOnly.isChecked();
	}

	public int getOpacity()
	{
		return opacity.get();
	}

	public int getFluidOpacity()
	{
		return fluidOpacity.get();
	}

	public Set<Block> whitelist()
	{
		return whitelist;
	}

	public List<String> whitelistNames()
	{
		return whitelist.stream()
			.map(b -> BuiltInRegistries.BLOCK.getKey(b).getPath()).sorted()
			.toList();
	}

	@Override
	public void onEnable()
	{
		refreshChunks();
	}

	@Override
	public void onDisable()
	{
		refreshChunks();
	}

	/**
	 * Forces a chunk re-mesh so the change is visible straight away.
	 * Handles both the vanilla pipeline and Sodium.
	 */
	private void refreshChunks()
	{
		if(net.novaclient.NovaClient.MC.levelRenderer != null)
			net.novaclient.NovaClient.MC.levelRenderer.allChanged();

		SodiumCompat.reloadChunks();
	}

	@Override
	public String getInfoString()
	{
		return SodiumCompat.isPresent() ? "sodium"
			: String.valueOf(whitelist.size());
	}
}
