/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.EnumSetting;
import net.novaclient.setting.IntSetting;
import net.novaclient.util.EntityUtils;

/** Ported from Meteor's KillAura. */
public final class KillAuraModule extends Module
{
	private final EnumSetting<RotationMode> rotation = new EnumSetting<>(
		"Rotate", "Determines when you should rotate towards the target.",
		RotationMode.values(), RotationMode.Always);

	private final BoolSetting onlyOnClick = new BoolSetting("Only on click",
		"Only attacks when holding left click.", false);

	private final BoolSetting onlyOnLook = new BoolSetting("Only on look",
		"Only attacks the entity you are looking at.", false);

	private final BoolSetting pauseOnUse = new BoolSetting("Pause on use",
		"Does not attack while using an item.", false);

	private final BoolSetting players = new BoolSetting("Players",
		"Attack other players.", true);
	private final BoolSetting hostile = new BoolSetting("Hostile mobs",
		"Attack hostile mobs.", false);
	private final BoolSetting passive = new BoolSetting("Passive mobs",
		"Attack passive mobs.", false);
	private final BoolSetting ignoreNamed = new BoolSetting("Ignore named",
		"Don't attack mobs with a custom name.", false);
	private final BoolSetting ignoreTamed = new BoolSetting("Ignore tamed",
		"Don't attack mobs you tamed.", true);
	private final BoolSetting ignorePassive = new BoolSetting("Ignore passive",
		"Only attack sometimes-passive mobs if they are targeting you.", true);

	private final DoubleSetting range = new DoubleSetting("Range",
		"The maximum range the entity can be to attack it.", 4.5, 0, 6, 0.05);

	private final DoubleSetting wallsRange = new DoubleSetting("Walls range",
		"The maximum range the entity can be attacked through walls.", 3.5, 0,
		6, 0.05);

	private final IntSetting maxTargets = new IntSetting("Max targets",
		"How many entities to target at once.", 1, 1, 5, 1);

	private final BoolSetting customDelay = new BoolSetting("Custom delay",
		"Use a custom delay instead of the vanilla cooldown.", false);

	private final IntSetting hitDelay = new IntSetting("Hit delay",
		"How fast you hit the entity in ticks.", 11, 0, 60, 1);

	private final EnumSetting<ShieldMode> shieldMode = new EnumSetting<>(
		"Shield mode", "What to do when your target is blocking with a"
			+ " shield.",
		ShieldMode.values(), ShieldMode.None);

	private final List<Entity> targets = new ArrayList<>();
	private int hitTimer;

	public KillAuraModule()
	{
		super("KillAura", "Attacks entities around you.",
			ModuleCategory.COMBAT);
		displayName("杀戮光环");

		addSetting(rotation);
		addSetting(onlyOnClick);
		addSetting(onlyOnLook);
		addSetting(pauseOnUse);
		addSetting(players);
		addSetting(hostile);
		addSetting(passive);
		addSetting(ignoreNamed);
		addSetting(ignoreTamed);
		addSetting(ignorePassive);
		addSetting(range);
		addSetting(wallsRange);
		addSetting(maxTargets);
		addSetting(customDelay);
		addSetting(hitDelay);
		addSetting(shieldMode);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	public Entity getTarget()
	{
		return targets.isEmpty() ? null : targets.get(0);
	}

	@Override
	public String getInfoString()
	{
		Entity target = getTarget();
		return target == null ? null : target.getName().getString();
	}

	private void onUpdate()
	{
		var mc = NovaClient.MC;
		var player = mc.player;

		if(!player.isAlive()
			|| mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;

		if(pauseOnUse.isChecked()
			&& (mc.gameMode.isDestroying() || player.isUsingItem()))
			return;

		if(onlyOnClick.isChecked() && !mc.options.keyAttack.isDown())
			return;

		targets.clear();

		if(onlyOnLook.isChecked())
		{
			Entity targeted = mc.crosshairPickEntity;
			if(targeted == null || !entityCheck(targeted))
				return;

			targets.add(targeted);
		}else
		{
			List<Entity> candidates = new ArrayList<>();
			for(Entity entity : EntityUtils.getAliveEntities(Entity.class))
				if(entityCheck(entity))
					candidates.add(entity);

			candidates.sort(Comparator
				.comparingDouble(e -> player.distanceToSqr(e)));

			int max = maxTargets.get();
			for(int i = 0; i < candidates.size() && i < max; i++)
				targets.add(candidates.get(i));
		}

		if(targets.isEmpty())
			return;

		Entity primary = targets.get(0);

		if(rotation.get() == RotationMode.Always)
			rotateTo(primary);

		if(!delayCheck())
			return;

		for(Entity target : targets)
			attack(target);
	}

	private void attack(Entity target)
	{
		if(rotation.get() == RotationMode.OnHit)
			rotateTo(target);

		NovaClient.MC.gameMode.attack(NovaClient.MC.player, target);
		NovaClient.MC.player.swing(InteractionHand.MAIN_HAND);

		hitTimer = 0;
	}

	private void rotateTo(Entity target)
	{
		var box = EntityUtils.getLerpedBox(target, 1);
		net.novaclient.util.RotationUtils
			.rotateTo(net.novaclient.util.RotationUtils.getEyesPos(),
				new net.novaclient.util.RotationUtils.AimTarget(
					box.getCenter()));
	}

	private boolean delayCheck()
	{
		if(customDelay.isChecked())
		{
			if(hitTimer < hitDelay.get())
			{
				hitTimer++;
				return false;
			}

			return true;
		}

		return NovaClient.MC.player.getAttackStrengthScale(0.5F) >= 1;
	}

	private boolean entityCheck(Entity entity)
	{
		var mc = NovaClient.MC;
		var player = mc.player;

		if(entity.equals(player) || entity.equals(mc.getCameraEntity()))
			return false;

		if(entity instanceof LivingEntity le && le.isDeadOrDying()
			|| !entity.isAlive())
			return false;

		AABB hitbox = entity.getBoundingBox();
		double dx = Mth.clamp(player.getX(), hitbox.minX, hitbox.maxX)
			- player.getX();
		double dy = Mth.clamp(player.getY(), hitbox.minY, hitbox.maxY)
			- player.getY();
		double dz = Mth.clamp(player.getZ(), hitbox.minZ, hitbox.maxZ)
			- player.getZ();

		if(dx * dx + dy * dy + dz * dz > range.get() * range.get())
			return false;

		if(!isTargetType(entity))
			return false;

		if(ignoreNamed.isChecked() && entity.hasCustomName())
			return false;

		if(!player.hasLineOfSight(entity))
		{
			double wallRange = wallsRange.get();
			if(entity.distanceToSqr(player) > wallRange * wallRange)
				return false;
		}

		if(ignoreTamed.isChecked()
			&& entity instanceof net.minecraft.world.entity.OwnableEntity ownable
			&& ownable.getOwner() != null && ownable.getOwner().equals(player))
			return false;

		if(ignorePassive.isChecked())
		{
			if(entity instanceof EnderMan enderman && !enderman.isCreepy())
				return false;

			if(entity instanceof AbstractPiglin
				&& !((net.minecraft.world.entity.Mob)entity).isAggressive())
				return false;
		}

		if(entity instanceof Player target)
		{
			if(target.isCreative())
				return false;

			if(shieldMode.get() == ShieldMode.Ignore && target.isBlocking())
				return false;
		}

		return true;
	}

	private boolean isTargetType(Entity entity)
	{
		if(entity instanceof Player)
			return players.isChecked();

		if(entity instanceof net.minecraft.world.entity.Mob mob)
		{
			if(mob instanceof EnderMan)
				return hostile.isChecked();

			return mob.isAggressive() ? hostile.isChecked()
				: passive.isChecked();
		}

		return false;
	}

	private boolean acceptableWeapon(ItemStack stack)
	{
		if(shouldShieldBreak())
			return stack.getItem() instanceof AxeItem;

		return true;
	}

	private boolean shouldShieldBreak()
	{
		if(shieldMode.get() != ShieldMode.Break)
			return false;

		for(Entity target : targets)
			if(target instanceof Player player && player.isBlocking())
				return true;

		return false;
	}

	public enum RotationMode
	{
		Always, OnHit, None;
	}

	public enum ShieldMode
	{
		Ignore, Break, None;
	}
}
