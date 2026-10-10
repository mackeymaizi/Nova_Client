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

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.render.RenderUtils;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.ColorSetting;
import net.novaclient.setting.IntSetting;

/**
 * Ported from Meteor's Waypoints. Lets you save positions and shows them
 * in the world. Managed with the {@code .waypoint} command.
 */
public final class WaypointsModule extends Module
{
	public record Waypoint(String name, Vec3 pos, String dimension)
	{}

	private final BoolSetting showBeams = new BoolSetting("Beams",
		"Draw a vertical beam at each waypoint.", true);

	private final BoolSetting showBoxes = new BoolSetting("Boxes",
		"Draw a box at each waypoint.", true);

	private final BoolSetting showDistance = new BoolSetting("Distance",
		"Include the distance in the label.", true);

	private final ColorSetting color = new ColorSetting("Color",
		"Waypoint colour.", new java.awt.Color(78, 72, 230));

	private final IntSetting beamHeight = new IntSetting("Beam height",
		"How tall the beam is.", 64, 8, 320, 8);

	private final List<Waypoint> waypoints = new ArrayList<>();

	public WaypointsModule()
	{
		super("Waypoints", "Saves and displays positions in the world.",
			ModuleCategory.RENDER);
		displayName("路径点");

		addSetting(showBeams);
		addSetting(showBoxes);
		addSetting(showDistance);
		addSetting(color);
		addSetting(beamHeight);

		subscribe(net.novaclient.event.events.RenderEvent.class,
			this::onRender);
	}

	public List<Waypoint> waypoints()
	{
		return waypoints;
	}

	public boolean add(String name, Vec3 pos)
	{
		if(waypoints.stream().anyMatch(w -> w.name().equalsIgnoreCase(name)))
			return false;

		String dimension = NovaClient.MC.level != null
			? NovaClient.MC.level.dimension().identifier().toString() : "";

		waypoints.add(new Waypoint(name, pos, dimension));
		return true;
	}

	public boolean remove(String name)
	{
		return waypoints.removeIf(w -> w.name().equalsIgnoreCase(name));
	}

	private void onRender(net.novaclient.event.events.RenderEvent event)
	{
		if(waypoints.isEmpty())
			return;

		List<RenderUtils.ColoredBox> boxes = new ArrayList<>();

		for(Waypoint waypoint : waypoints)
		{
			Vec3 pos = waypoint.pos();

			if(showBeams.isChecked())
			{
				AABB beam = new AABB(pos.x - 0.15, pos.y, pos.z - 0.15,
					pos.x + 0.15, pos.y + beamHeight.get(), pos.z + 0.15);
				boxes.add(new RenderUtils.ColoredBox(beam,
					color.getPacked(0.4F)));
			}

			if(showBoxes.isChecked())
			{
				AABB box = new AABB(pos.x - 0.5, pos.y, pos.z - 0.5, pos.x + 0.5,
					pos.y + 1, pos.z + 0.5);
				boxes.add(new RenderUtils.ColoredBox(box,
					color.getPacked(0.9F)));
			}
		}

		RenderUtils.drawOutlinedBoxes(event.poseStack(), boxes, false);
	}

	@Override
	public String getInfoString()
	{
		return String.valueOf(waypoints.size());
	}
}
