/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.render.RenderUtils;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.ColorSetting;
import net.novaclient.setting.IntSetting;
import net.novaclient.setting.StringSetting;
import net.novaclient.util.ChatUtils;

/**
 * Ported from Wurst's SearchHack. Finds a chosen block type around you and
 * highlights it in the world.
 *
 * <p>This is the <em>highlight</em> half of the pair - see
 * {@link XRayModule} for hiding blocks instead.
 */
public final class SearchModule extends Module
{
	private final StringSetting blockName = new StringSetting("Block",
		"The block id to search for, e.g. diamond_ore.", "diamond_ore");

	private final IntSetting range = new IntSetting("Range",
		"Radius in blocks to search.", 32, 4, 128, 4);

	private final IntSetting limit = new IntSetting("Limit",
		"Maximum number of blocks to display.", 64, 1, 512, 1);

	private final BoolSetting tracers = new BoolSetting("Tracers",
		"Draw a line to each found block.", false);

	private final BoolSetting notify = new BoolSetting("Notify",
		"Print a chat message when new blocks are found.", false);

	private final ColorSetting color = new ColorSetting("Color",
		"Highlight colour.", new java.awt.Color(255, 209, 102));

	private final List<BlockPos> found = new ArrayList<>();
	private Block target;
	private int timer;

	public SearchModule()
	{
		super("Search", "Highlights a chosen block type around you.",
			ModuleCategory.RENDER);
		displayName("方块搜寻");

		addSetting(blockName);
		addSetting(range);
		addSetting(limit);
		addSetting(tracers);
		addSetting(notify);
		addSetting(color);

		subscribe(UpdateEvent.class, e -> onUpdate());
		subscribe(net.novaclient.event.events.RenderEvent.class,
			this::onRender);
	}

	@Override
	public void onEnable()
	{
		found.clear();
		timer = 0;
		resolveTarget();
	}

	private void resolveTarget()
	{
		Identifier id = Identifier.withDefaultNamespace(blockName.get().trim());
		Block block = BuiltInRegistries.BLOCK.getValue(id);

		if(block == null || block == Blocks.AIR)
		{
			ChatUtils.error("Unknown block \"" + blockName.get() + "\".");
			target = null;
			return;
		}

		target = block;
	}

	private void onUpdate()
	{
		if(timer > 0)
		{
			timer--;
			return;
		}

		timer = 20;

		if(target == null || NovaClient.MC.level == null
			|| NovaClient.MC.player == null)
			return;

		BlockPos origin = BlockPos.containing(
			NovaClient.MC.player.position());
		int r = range.get();

		List<BlockPos> results = new ArrayList<>();

		for(BlockPos pos : BlockPos.betweenClosed(origin.offset(-r, -r, -r),
			origin.offset(r, r, r)))
		{
			BlockPos immutable = pos.immutable();

			if(immutable.distSqr(origin) > (double)r * r)
				continue;

			if(!NovaClient.MC.level.getBlockState(immutable).is(target))
				continue;

			results.add(immutable);

			if(results.size() >= limit.get())
				break;
		}

		int previous = found.size();
		found.clear();
		found.addAll(results);

		if(notify.isChecked() && found.size() > previous && !found.isEmpty())
			ChatUtils.nova("Found " + found.size() + " x "
				+ blockName.get() + ".");
	}

	private void onRender(net.novaclient.event.events.RenderEvent event)
	{
		if(found.isEmpty())
			return;

		List<RenderUtils.ColoredBox> boxes = new ArrayList<>(found.size());

		for(BlockPos pos : found)
			boxes.add(new RenderUtils.ColoredBox(
				new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1,
					pos.getY() + 1, pos.getZ() + 1),
				color.getPacked(0.9F)));

		RenderUtils.drawOutlinedBoxes(event.poseStack(), boxes, false);

		if(tracers.isChecked())
		{
			List<net.minecraft.world.phys.Vec3> ends = new ArrayList<>();
			List<Integer> colors = new ArrayList<>();

			for(BlockPos pos : found)
			{
				ends.add(net.minecraft.world.phys.Vec3.atCenterOf(pos));
				colors.add(color.getPacked(0.6F));
			}

			RenderUtils.drawTracers(event.poseStack(), event.tickDelta(), ends,
				colors, false);
		}
	}

	@Override
	public String getInfoString()
	{
		return blockName.get() + " " + found.size();
	}
}
