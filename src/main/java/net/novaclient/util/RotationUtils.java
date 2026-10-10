/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;

public enum RotationUtils
{
	;

	public static Vec3 getEyesPos()
	{
		return NovaClient.MC.player.getEyePosition();
	}

	public static float getYaw(Vec3 from, Vec3 to)
	{
		double dx = to.x - from.x;
		double dz = to.z - from.z;

		return (float)Mth.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90);
	}

	public static float getPitch(Vec3 from, Vec3 to)
	{
		double dx = to.x - from.x;
		double dy = to.y - from.y;
		double dz = to.z - from.z;
		double horizontal = Math.sqrt(dx * dx + dz * dz);

		return (float)Mth
			.wrapDegrees(-Math.toDegrees(Math.atan2(dy, horizontal)));
	}

	public static float getYaw(Entity entity, float partialTicks)
	{
		return getYaw(getEyesPos(), EntityUtils.getLerpedBox(entity,
			partialTicks).getCenter());
	}

	public static float getPitch(Entity entity, float partialTicks)
	{
		return getPitch(getEyesPos(),
			EntityUtils.getLerpedBox(entity, partialTicks).getCenter());
	}

	/** Client-side rotation. The packets pick this up automatically. */
	public static void rotate(float yaw, float pitch)
	{
		NovaClient.MC.player.setYRot(yaw);
		NovaClient.MC.player.setXRot(pitch);
	}

	public static void rotateTo(Entity entity)
	{
		rotateTo(getEyesPos(), getAimTarget(entity));
	}

	public static void rotateTo(Vec3 eyes, AimTarget aim)
	{
		rotate(getYaw(eyes, aim.point()), getPitch(eyes, aim.point()));
	}

	public record AimTarget(Vec3 point)
	{}

	public static AimTarget getAimTarget(Entity entity)
	{
		return new AimTarget(entity.getBoundingBox().getCenter());
	}
}
