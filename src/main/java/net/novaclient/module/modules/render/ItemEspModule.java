/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.novaclient.event.events.CameraBobEvent;
import net.novaclient.event.events.RenderEvent;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.render.RenderUtils;
import net.novaclient.setting.ColorSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.EnumSetting;
import net.novaclient.util.EntityUtils;

/** Ported from Wurst's ItemEspHack. */
public final class ItemEspModule extends Module
{
	private final EnumSetting<EspStyle> style = new EnumSetting<>("Style",
		"How to highlight the items.", EspStyle.values(),
		EspStyle.LINES_AND_BOXES);

	private final DoubleSetting boxSize = new DoubleSetting("Box size",
		"0 = accurate mode, showing the exact hitbox of each item.\n"
			+ "Higher values show larger boxes that look better.",
		0, 0, 1, 0.05);

	private final ColorSetting color = new ColorSetting("Color",
		"Items will be highlighted in this color.", Color.YELLOW);

	private final List<ItemEntity> items = new ArrayList<>();

	public ItemEspModule()
	{
		super("ItemESP", "Highlights dropped items.", ModuleCategory.RENDER);
		displayName("物品透视");

		addSetting(style);
		addSetting(boxSize);
		addSetting(color);

		subscribe(UpdateEvent.class, e -> {
			items.clear();
			items.addAll(EntityUtils.getItemEntities());
		});

		subscribe(CameraBobEvent.class, e -> {
			if(style.get().hasLines())
				e.cancel();
		});

		subscribe(RenderEvent.class, e -> onRender(e));
	}

	private void onRender(RenderEvent event)
	{
		float partialTicks = event.tickDelta();
		int lineColor = color.getPacked(0x80);
		double extraSize = boxSize.get() / 2;

		if(style.get().hasBoxes())
		{
			List<AABB> boxes = new ArrayList<>(items.size());
			for(ItemEntity e : items)
				boxes.add(EntityUtils.getLerpedBox(e, partialTicks)
					.move(0, extraSize, 0).inflate(extraSize));

			RenderUtils.drawOutlinedBoxes(event.poseStack(), boxes, lineColor,
				false);
		}

		if(style.get().hasLines())
		{
			List<Vec3> ends = new ArrayList<>(items.size());
			for(ItemEntity e : items)
				ends.add(EntityUtils.getLerpedBox(e, partialTicks).getCenter());

			RenderUtils.drawTracers(event.poseStack(), partialTicks, ends,
				lineColor, false);
		}
	}
}
