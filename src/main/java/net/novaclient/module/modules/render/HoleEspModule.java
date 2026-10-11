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
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.render.RenderUtils;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.ColorSetting;
import net.novaclient.setting.IntSetting;

/**
 * Ported from Meteor's HoleESP. Highlights 1x1 holes that protect you from
 * crystal and anchor explosions.
 */
public final class HoleEspModule extends Module
{
	private final IntSetting range = new IntSetting("Range",
		"Radius to scan for holes.", 6, 1, 16, 1);

	private final IntSetting verticalRange = new IntSetting("Vertical radius",
		"Vertical radius to scan for holes.", 6, 1, 16, 1);

	private final IntSetting minHeight = new IntSetting("Min height",
		"Minimum number of air blocks above a hole for it to count.", 3, 3, 8, 1);

	private final BoolSetting ignoreOwn = new BoolSetting("Ignore own hole",
		"Skip the hole you are currently standing in.", false);

	private final BoolSetting bedrockHoles = new BoolSetting("Bedrock holes",
		"Show holes made entirely of bedrock.", true);

	private final BoolSetting obsidianHoles = new BoolSetting("Obsidian holes",
		"Show holes made of obsidian.", true);

	private final BoolSetting renderTracers = new BoolSetting("Tracers",
		"Draw a line from you to each hole.", false);

	private final ColorSetting bedrockColor = new ColorSetting(
		"Bedrock color", "Colour for bedrock holes.",
		new java.awt.Color(80, 255, 120));

	private final ColorSetting obsidianColor = new ColorSetting(
		"Obsidian color", "Colour for obsidian holes.",
		new java.awt.Color(180, 100, 255));

	private final List<BlockPos> holes = new ArrayList<>();
	private final List<BlockPos> bedrock = new ArrayList<>();
	private int timer;

	public HoleEspModule()
	{
		super("HoleESP", "Highlights safe 1x1 holes around you.",
			ModuleCategory.RENDER);
		displayName("安全洞透视");

		addSetting(range);
		addSetting(verticalRange);
		addSetting(minHeight);
		addSetting(ignoreOwn);
		addSetting(bedrockHoles);
		addSetting(obsidianHoles);
		addSetting(renderTracers);
		addSetting(bedrockColor);
		addSetting(obsidianColor);

		subscribe(UpdateEvent.class, e -> onUpdate());
		subscribe(net.novaclient.event.events.RenderEvent.class,
			this::onRender);
	}

	private void onUpdate()
	{
		if(timer > 0)
		{
			timer--;
			return;
		}

		timer = 5;

		holes.clear();
		bedrock.clear();

		var player = NovaClient.MC.player;
		var level = NovaClient.MC.level;

		if(player == null || level == null)
			return;

		BlockPos origin = BlockPos.containing(player.position());
		int r = range.get();
		int vy = verticalRange.get();
		int neededHeight = minHeight.get();

		for(BlockPos pos : BlockPos.betweenClosed(origin.offset(-r, -vy, -r),
			origin.offset(r, vy, r)))
		{
			BlockPos immutable = pos.immutable();

			if(immutable.distSqr(origin) > (double)r * r)
				continue;

			if(ignoreOwn.isChecked()
				&& immutable.getX() == origin.getX()
				&& immutable.getZ() == origin.getZ())
				continue;

			HoleType type = checkHole(level, immutable, neededHeight);
			if(type == null)
				continue;

			if(type == HoleType.Bedrock)
			{
				if(bedrockHoles.isChecked())
					bedrock.add(immutable);
			}else if(obsidianHoles.isChecked())
				holes.add(immutable);
		}
	}

	/** Returns the hole type, or null when this isn't a safe hole. */
	private HoleType checkHole(net.minecraft.world.level.Level level,
		BlockPos pos, int neededHeight)
	{
		if(!level.getBlockState(pos).isAir())
			return null;

		// Require the configured number of air blocks above the hole.
		for(int i = 1; i < neededHeight; i++)
			if(!level.getBlockState(pos.above(i)).isAir())
				return null;

		if(level.getBlockState(pos.below()).isAir())
			return null;

		boolean allBedrock = true;

		for(Direction direction : new Direction[]{Direction.NORTH,
			Direction.SOUTH, Direction.EAST, Direction.WEST})
		{
			var state = level.getBlockState(pos.relative(direction));

			if(state.isAir()
				|| !state.getFluidState().isEmpty())
				return null;

			if(!state.is(net.minecraft.world.level.block.Blocks.BEDROCK))
				allBedrock = false;
		}

		return allBedrock ? HoleType.Bedrock : HoleType.Obsidian;
	}

	private void onRender(net.novaclient.event.events.RenderEvent event)
	{
		List<RenderUtils.ColoredBox> boxes = new ArrayList<>();

		int bedrockRgb = bedrockColor.getPacked(0.35F);
		int obsidianRgb = obsidianColor.getPacked(0.35F);

		for(BlockPos pos : bedrock)
			boxes.add(new RenderUtils.ColoredBox(box(pos), bedrockRgb));

		for(BlockPos pos : holes)
			boxes.add(new RenderUtils.ColoredBox(box(pos), obsidianRgb));

		if(boxes.isEmpty())
			return;

		RenderUtils.drawOutlinedBoxes(event.poseStack(), boxes, false);

		if(renderTracers.isChecked())
		{
			List<net.minecraft.world.phys.Vec3> ends = new ArrayList<>();
			List<Integer> colors = new ArrayList<>();

			for(BlockPos pos : bedrock)
			{
				ends.add(net.minecraft.world.phys.Vec3.atCenterOf(pos));
				colors.add(bedrockColor.getPacked(0.6F));
			}

			for(BlockPos pos : holes)
			{
				ends.add(net.minecraft.world.phys.Vec3.atCenterOf(pos));
				colors.add(obsidianColor.getPacked(0.6F));
			}

			RenderUtils.drawTracers(event.poseStack(), event.tickDelta(), ends,
				colors, false);
		}
	}

	private AABB box(BlockPos pos)
	{
		return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1,
			pos.getY() + 1, pos.getZ() + 1);
	}

	@Override
	public String getInfoString()
	{
		return String.valueOf(holes.size() + bedrock.size());
	}

	private enum HoleType
	{
		Bedrock, Obsidian;
	}
}
