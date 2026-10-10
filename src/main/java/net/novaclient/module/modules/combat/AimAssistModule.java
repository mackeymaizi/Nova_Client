/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.combat;

import java.util.Comparator;
import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.EnumSetting;
import net.novaclient.util.EntityUtils;
import net.novaclient.util.RotationUtils;

/** Ported from Wurst's AimAssistHack. Smoothly aims at nearby entities. */
public final class AimAssistModule extends Module
{
	private final DoubleSetting range = new DoubleSetting("Range",
		"Maximum distance to aim at.", 4.5, 1, 6, 0.05);

	private final DoubleSetting rotationSpeed = new DoubleSetting(
		"Rotation speed", "Degrees per second.", 600, 10, 3600, 10);

	private final DoubleSetting fov = new DoubleSetting("FOV",
		"Only targets inside this field of view.", 120, 30, 360, 10);

	private final BoolSetting checkLos = new BoolSetting(
		"Check line of sight", "Requires a clear line of sight.", true);

	private final BoolSetting ignorePassive = new BoolSetting("Ignore passive",
		"Skips passive mobs.", true);

	private final EnumSetting<AimAt> aimAt = new EnumSetting<>("Aim at",
		"Which point of the hitbox to aim at.", AimAt.values(), AimAt.Center);

	public AimAssistModule()
	{
		super("AimAssist", "Smoothly aims at nearby entities for you.",
			ModuleCategory.COMBAT);
		displayName("瞄准辅助");

		addSetting(range);
		addSetting(rotationSpeed);
		addSetting(fov);
		addSetting(checkLos);
		addSetting(ignorePassive);
		addSetting(aimAt);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player == null)
			return;

		List<Entity> candidates = EntityUtils.getAliveEntities(Entity.class,
			e -> e != player && e instanceof LivingEntity
				&& !(e instanceof Player p && p.isCreative()));

		candidates.removeIf(e -> !isValid(e));

		Entity target = candidates.stream()
			.min(Comparator.comparingDouble(e -> player.distanceToSqr(e)))
			.orElse(null);

		if(target == null)
			return;

		Vec3 point = aimPoint(target);
		Vec3 eyes = RotationUtils.getEyesPos();

		float targetYaw = RotationUtils.getYaw(eyes, point);
		float targetPitch = RotationUtils.getPitch(eyes, point);

		float maxStep = (float)(rotationSpeed.get() / 20.0);
		float yaw = approach(player.getYRot(), targetYaw, maxStep);
		float pitch = approach(player.getXRot(), targetPitch, maxStep);

		player.setYRot(yaw);
		player.setXRot(pitch);
	}

	private boolean isValid(Entity entity)
	{
		var player = NovaClient.MC.player;

		if(entity.distanceToSqr(player) > range.get() * range.get())
			return false;

		if(checkLos.isChecked() && !player.hasLineOfSight(entity))
			return false;

		if(ignorePassive.isChecked()
			&& entity instanceof net.minecraft.world.entity.Mob mob
			&& !mob.isAggressive()
			&& !(entity instanceof Player))
			return false;

		Vec3 eyes = RotationUtils.getEyesPos();
		float yaw = RotationUtils.getYaw(eyes, entity.getBoundingBox().getCenter());

		float delta = Math.abs(net.minecraft.util.Mth.wrapDegrees(
			yaw - player.getYRot()));

		return delta <= fov.get() / 2.0F;
	}

	private Vec3 aimPoint(Entity entity)
	{
		var box = entity.getBoundingBox();

		return switch(aimAt.get())
		{
			case Head -> new Vec3(box.getCenter().x, box.maxY - 0.1, box.getCenter().z);
			case Body -> new Vec3(box.getCenter().x, box.minY + box.getYsize() * 0.6,
				box.getCenter().z);
			case Feet -> new Vec3(box.getCenter().x, box.minY + 0.1, box.getCenter().z);
			case Center -> box.getCenter();
		};
	}

	/** Moves {@code current} toward {@code target} by at most {@code maxStep}. */
	private float approach(float current, float target, float maxStep)
	{
		float delta = net.minecraft.util.Mth.wrapDegrees(target - current);

		if(Math.abs(delta) <= maxStep)
			return target;

		return current + Math.copySign(maxStep, delta);
	}

	public enum AimAt
	{
		Center, Head, Body, Feet;
	}
}
