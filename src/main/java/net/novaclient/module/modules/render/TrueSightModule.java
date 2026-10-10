/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.novaclient.event.events.InvisibleToEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;

/** Ported from Wurst's TrueSightHack. */
public final class TrueSightModule extends Module
{
	private final BoolSetting players =
		new BoolSetting("Players", "Show invisible players.", true);
	private final BoolSetting hostile = new BoolSetting("Hostile mobs",
		"Show invisible hostile mobs.", true);
	private final BoolSetting neutral = new BoolSetting("Neutral mobs",
		"Show invisible neutral mobs.", true);
	private final BoolSetting passive = new BoolSetting("Passive mobs",
		"Show invisible passive mobs.", true);
	private final BoolSetting other = new BoolSetting("Other entities",
		"Show any other invisible entity.", true);
	private final BoolSetting namedOnly = new BoolSetting("Named only",
		"Only show invisible entities that have a custom name.", false);

	public TrueSightModule()
	{
		super("TrueSight", "Renders invisible entities as ghosts.",
			ModuleCategory.RENDER);
		displayName("透视视野");

		addSetting(players);
		addSetting(hostile);
		addSetting(neutral);
		addSetting(passive);
		addSetting(other);
		addSetting(namedOnly);

		subscribe(InvisibleToEvent.class, e -> {
			if(shouldBeVisible(e.entity()))
				e.setInvisible(false);
		});
	}

	public boolean shouldBeVisible(Entity entity)
	{
		if(!isEnabled())
			return false;

		if(namedOnly.isChecked() && !entity.hasCustomName())
			return false;

		if(entity instanceof RemotePlayer)
			return players.isChecked();

		if(entity instanceof AbstractPiglin || entity instanceof Piglin)
			return neutral.isChecked();

		if(entity instanceof NeutralMob)
			return neutral.isChecked();

		if(entity instanceof Slime || entity instanceof Bat)
			return passive.isChecked();

		if(entity instanceof TamableAnimal || entity instanceof Animal)
			return passive.isChecked();

		if(entity instanceof Villager || entity instanceof ZombieVillager
			|| entity instanceof IronGolem || entity instanceof SnowGolem)
			return passive.isChecked();

		if(entity instanceof EnderMan || entity instanceof Shulker)
			return neutral.isChecked();

		if(entity instanceof Mob)
			return hostile.isChecked();

		if(entity instanceof ArmorStand)
			return other.isChecked();

		return other.isChecked();
	}
}
