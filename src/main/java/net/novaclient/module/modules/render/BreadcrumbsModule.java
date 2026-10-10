/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.render.RenderUtils;
import net.novaclient.setting.ColorSetting;
import net.novaclient.setting.IntSetting;

/** Ported from Meteor's Breadcrumbs. Draws a trail behind you. */
public final class BreadcrumbsModule extends Module
{
	private final IntSetting maxPoints = new IntSetting("Max points",
		"Maximum number of trail points to keep.", 200, 10, 2000, 10);

	private final ColorSetting color = new ColorSetting("Color",
		"Trail colour.", new java.awt.Color(37, 99, 235));

	private final IntSetting minDistance = new IntSetting("Min distance",
		"Minimum distance between recorded points, in centimetres.", 50, 1, 500,
		10);

	private final Deque<Vec3> points = new ArrayDeque<>();
	private Vec3 last;

	public BreadcrumbsModule()
	{
		super("Breadcrumbs", "Draws a trail showing where you have been.",
			ModuleCategory.RENDER);
		displayName("足迹");

		addSetting(maxPoints);
		addSetting(color);
		addSetting(minDistance);

		subscribe(UpdateEvent.class, e -> onUpdate());
		subscribe(net.novaclient.event.events.RenderEvent.class,
			this::onRender);
	}

	@Override
	public void onEnable()
	{
		points.clear();
		last = null;
	}

	@Override
	public void onDisable()
	{
		points.clear();
		last = null;
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player == null)
			return;

		Vec3 pos = player.position();

		if(last != null
			&& last.distanceToSqr(pos) < Math.pow(minDistance.get() / 100.0, 2))
			return;

		last = pos;
		points.addLast(pos);

		while(points.size() > maxPoints.get())
			points.removeFirst();
	}

	private void onRender(net.novaclient.event.events.RenderEvent event)
	{
		if(points.size() < 2)
			return;

		List<Vec3> list = new ArrayList<>(points);
		RenderUtils.drawCurvedLine(event.poseStack(), list,
			color.getPacked(0.8F), false);
	}

	@Override
	public String getInfoString()
	{
		return String.valueOf(points.size());
	}
}
