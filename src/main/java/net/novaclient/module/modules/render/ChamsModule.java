/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import net.minecraft.world.entity.player.Player;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.ColorSetting;
import net.novaclient.setting.IntSetting;

/**
 * Ported from Meteor's Chams. Draws a coloured outline around entities so you
 * can see them through walls.
 */
public final class ChamsModule extends Module
{
	private final BoolSetting players = new BoolSetting("Players",
		"Outline other players.", true);

	private final BoolSetting mobs = new BoolSetting("Mobs",
		"Outline hostile mobs.", false);

	private final BoolSetting friends = new BoolSetting("Friends",
		"Outline friends in a different colour.", true);

	private final ColorSetting playerColor = new ColorSetting("Player color",
		"Outline colour for players.", new java.awt.Color(255, 80, 80));

	private final ColorSetting friendColor = new ColorSetting("Friend color",
		"Outline colour for friends.", new java.awt.Color(80, 255, 120));

	private final ColorSetting mobColor = new ColorSetting("Mob color",
		"Outline colour for mobs.", new java.awt.Color(255, 190, 60));

	private final IntSetting interval = new IntSetting("Interval",
		"Ticks between refreshes.", 5, 1, 40, 1);

	private int timer;

	public ChamsModule()
	{
		super("Chams", "Draws a coloured outline around entities, visible"
			+ " through walls.", ModuleCategory.RENDER);
		displayName("人物描边");

		addSetting(players);
		addSetting(mobs);
		addSetting(friends);
		addSetting(playerColor);
		addSetting(friendColor);
		addSetting(mobColor);
		addSetting(interval);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	@Override
	public void onDisable()
	{
		for(var entity : NovaClient.MC.level.entitiesForRendering())
			entity.setGlowingTag(false);
	}

	private void onUpdate()
	{
		if(timer > 0)
		{
			timer--;
			return;
		}

		timer = interval.get();

		if(NovaClient.MC.level == null)
			return;

		for(var entity : NovaClient.MC.level.entitiesForRendering())
		{
			if(entity == NovaClient.MC.player)
				continue;

			boolean shouldGlow = false;

			if(entity instanceof Player player)
				shouldGlow = players.isChecked()
					|| (friends.isChecked()
						&& net.novaclient.util.FriendManager
							.isFriend(player.getScoreboardName()));
			else if(entity instanceof net.minecraft.world.entity.Mob)
				shouldGlow = mobs.isChecked();

			entity.setGlowingTag(shouldGlow);
		}
	}

	/** Outline colour used by the glow renderer. */
	public int getOutlineColor(net.minecraft.world.entity.Entity entity)
	{
		if(entity instanceof Player player
			&& net.novaclient.util.FriendManager
				.isFriend(player.getScoreboardName()))
			return friendColor.getRGB();

		if(entity instanceof Player)
			return playerColor.getRGB();

		return mobColor.getRGB();
	}

	public boolean outlinesPlayers()
	{
		return isEnabled() && (players.isChecked() || friends.isChecked());
	}

	public boolean outlinesMobs()
	{
		return isEnabled() && mobs.isChecked();
	}
}
