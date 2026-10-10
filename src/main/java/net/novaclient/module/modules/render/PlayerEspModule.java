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

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.CameraBobEvent;
import net.novaclient.event.events.RenderEvent;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.render.RenderUtils;
import net.novaclient.render.RenderUtils.ColoredBox;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.EnumSetting;
import net.novaclient.util.EntityUtils;

/** Ported from Wurst's PlayerEspHack. */
public final class PlayerEspModule extends Module
{
	private final EnumSetting<EspStyle> style = new EnumSetting<>("Style",
		"How to highlight the players.", EspStyle.values(),
		EspStyle.LINES_AND_BOXES);

	private final DoubleSetting boxSize = new DoubleSetting("Box size",
		"0 = accurate mode, showing the exact hitbox of each player.\n"
			+ "Higher values show larger boxes that look better.",
		0, 0, 1, 0.05);

	private final BoolSetting filterInvisible =
		new BoolSetting("Ignore invisible",
			"Don't highlight players that are invisible.", true);

	private final BoolSetting filterSleeping =
		new BoolSetting("Ignore sleeping",
			"Don't highlight players that are sleeping.", true);

	private final List<Player> players = new ArrayList<>();

	public PlayerEspModule()
	{
		super("PlayerESP", "Highlights other players.", ModuleCategory.RENDER);
		displayName("玩家透视");

		addSetting(style);
		addSetting(boxSize);
		addSetting(filterInvisible);
		addSetting(filterSleeping);

		subscribe(UpdateEvent.class, e -> updatePlayers());

		subscribe(CameraBobEvent.class, e -> {
			if(style.get().hasLines())
				e.cancel();
		});

		subscribe(RenderEvent.class, e -> onRender(e));
	}

	private void updatePlayers()
	{
		players.clear();

		for(Player player : EntityUtils.getPlayers())
		{
			if(Math.abs(player.getY() - NovaClient.MC.player.getY()) > 1e6)
				continue;

			if(filterSleeping.isChecked() && player.isSleeping())
				continue;

			if(filterInvisible.isChecked() && player.isInvisible())
				continue;

			players.add(player);
		}
	}

	private void onRender(RenderEvent event)
	{
		float partialTicks = event.tickDelta();
		double extraSize = boxSize.get() / 2;

		if(style.get().hasBoxes())
		{
			List<ColoredBox> boxes = new ArrayList<>(players.size());
			for(Player e : players)
			{
				AABB box = EntityUtils.getLerpedBox(e, partialTicks)
					.move(0, extraSize, 0).inflate(extraSize);
				boxes.add(new ColoredBox(box, getColor(e)));
			}

			RenderUtils.drawOutlinedBoxes(event.poseStack(), boxes, false);
		}

		if(style.get().hasLines())
		{
			List<Vec3> ends = new ArrayList<>(players.size());
			List<Integer> colors = new ArrayList<>(players.size());
			for(Player e : players)
			{
				ends.add(
					EntityUtils.getLerpedBox(e, partialTicks).getCenter());
				colors.add(getColor(e));
			}

			RenderUtils.drawTracers(event.poseStack(), partialTicks, ends,
				colors, false);
		}
	}

	private int getColor(Player e)
	{
		float f = NovaClient.MC.player.distanceTo(e) / 20F;
		float r = Mth.clamp(2 - f, 0, 1);
		float g = Mth.clamp(f, 0, 1);
		return RenderUtils.toIntColor(new float[]{r, g, 0}, 0.5F);
	}
}
