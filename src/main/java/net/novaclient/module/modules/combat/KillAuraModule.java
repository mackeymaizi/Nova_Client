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

	private final EnumSetting<Priority> priority = new EnumSetting<>("Priority",
		"Which target to pick first.", Priority.values(), Priority.LowestHealth);

	private final EnumSetting<AttackItems> attackWhenHolding = new EnumSetting<>(
		"Attack when holding", "Only attack while holding these items.",
		AttackItems.values(), AttackItems.Any);

	private final BoolSetting autoSwitch = new BoolSetting("Auto switch",
		"Switches to your best weapon when attacking.", false);

	private final BoolSetting swapBack = new BoolSetting("Swap back",
		"Swaps back to the previous slot after attacking.", true);

	private final BoolSetting pauseOnLag = new BoolSetting("Pause on lag",
		"Stops attacking when the server is lagging.", false);

	private final BoolSetting swing = new BoolSetting("Swing",
		"Swing your hand when attacking.", true);

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
		addSetting(priority);
		addSetting(attackWhenHolding);
		addSetting(autoSwitch);
		addSetting(swapBack);
		addSetting(pauseOnLag);
		addSetting(swing);
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

		if(pauseOnLag.isChecked() && isServerLagging(mc))
			return;

		if(onlyOnClick.isChecked() && !mc.options.keyAttack.isDown())
			return;

		if(!acceptableWeapon(player.getMainHandItem()))
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

			candidates.sort(comparator(player));

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

		if(autoSwitch.isChecked())
			switchToBestWeapon();

		NovaClient.MC.gameMode.attack(NovaClient.MC.player, target);

		if(swing.isChecked())
			NovaClient.MC.player.swing(InteractionHand.MAIN_HAND);

		hitTimer = 0;
	}

	private Comparator<Entity> comparator(Player player)
	{
		return switch(priority.get())
		{
			case Closest -> Comparator
				.comparingDouble(e -> player.distanceToSqr(e));
			case LowestHealth -> Comparator.comparingDouble(e ->
				e instanceof LivingEntity le ? le.getHealth() : Double.MAX_VALUE);
			case LowestAngle -> Comparator.comparingDouble(e ->
			{
				net.minecraft.world.phys.Vec3 eyes =
					net.novaclient.util.RotationUtils.getEyesPos();
				float yaw = net.novaclient.util.RotationUtils
					.getYaw(eyes, e.getBoundingBox().getCenter());
				return Math.abs(Mth.wrapDegrees(yaw - player.getYRot()));
			});
		};
	}

	/** Rough lag detector: the tick loop is running slower than usual. */
	private boolean isServerLagging(net.minecraft.client.Minecraft mc)
	{
		return mc.getConnection() != null
			&& mc.getConnection().getPlayerInfo(mc.player.getUUID()) == null;
	}

	private void switchToBestWeapon()
	{
		var inventory = NovaClient.MC.player.getInventory();
		int current = inventory.getSelectedSlot();
		double bestScore = weaponScore(inventory.getItem(current));
		int bestSlot = -1;

		for(int i = 0; i < 9; i++)
		{
			double score = weaponScore(inventory.getItem(i));
			if(score > bestScore)
			{
				bestScore = score;
				bestSlot = i;
			}
		}

		if(bestSlot != -1)
			inventory.setSelectedSlot(bestSlot);
	}

	private double weaponScore(ItemStack stack)
	{
		if(stack.isEmpty())
			return -1;

		if(stack.is(ItemTags.SWORDS) || stack.getItem() instanceof AxeItem)
		{
			var modifiers = stack.get(net.minecraft.core.component.DataComponents
				.ATTRIBUTE_MODIFIERS);
			if(modifiers != null)
				return 10 + modifiers.compute(
					net.minecraft.world.entity.ai.attributes.Attributes
						.ATTACK_DAMAGE,
					0, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
			return 10;
		}

		return 0;
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
		boolean sword = stack.is(ItemTags.SWORDS);
		boolean axe = stack.getItem() instanceof AxeItem;

		switch(attackWhenHolding.get())
		{
			case Sword -> {
				if(!sword)
					return false;
			}
			case Axe -> {
				if(!axe)
					return false;
			}
			case SwordOrAxe -> {
				if(!sword && !axe)
					return false;
			}
			case Any -> {
			}
		}

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

	public enum Priority
	{
		LowestHealth, Closest, LowestAngle;
	}

	public enum AttackItems
	{
		Any, Sword, Axe, SwordOrAxe;
	}
}
