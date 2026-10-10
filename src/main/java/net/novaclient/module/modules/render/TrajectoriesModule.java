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
import java.util.function.Predicate;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.RenderEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.render.RenderUtils;
import net.novaclient.setting.ColorSetting;
import net.novaclient.util.BlockUtils;
import net.novaclient.util.EntityUtils;

/** Ported from Wurst's TrajectoriesHack. */
public final class TrajectoriesModule extends Module
{
	private final ColorSetting missColor = new ColorSetting("Miss color",
		"Color of the trajectory when it doesn't hit anything.", Color.GRAY);

	private final ColorSetting entityHitColor = new ColorSetting(
		"Entity hit color",
		"Color of the trajectory when it hits an entity.", Color.RED);

	private final ColorSetting blockHitColor = new ColorSetting(
		"Block hit color", "Color of the trajectory when it hits a block.",
		Color.GREEN);

	public TrajectoriesModule()
	{
		super("Trajectories", "Shows the path that thrown projectiles will"
			+ " take.", ModuleCategory.RENDER);
		displayName("弹道预测");

		addSetting(missColor);
		addSetting(entityHitColor);
		addSetting(blockHitColor);

		subscribe(RenderEvent.class, e -> onRender(e));
	}

	private void onRender(RenderEvent event)
	{
		Trajectory trajectory = getTrajectory(event.tickDelta());
		if(trajectory.isEmpty())
			return;

		ColorSetting color = getColor(trajectory);
		int lineColor = color.getPacked(0xC0);
		int quadColor = color.getPacked(0x40);

		AABB endBox = trajectory.getEndBox();

		RenderUtils.drawSolidBox(event.poseStack(), endBox, quadColor, false);
		RenderUtils.drawOutlinedBox(event.poseStack(), endBox, lineColor,
			false);
		RenderUtils.drawCurvedLine(event.poseStack(), trajectory.path(),
			lineColor, false);
	}

	private Trajectory getTrajectory(float partialTicks)
	{
		LocalPlayer player = NovaClient.MC.player;
		List<Vec3> path = new ArrayList<>();
		HitResult.Type type = HitResult.Type.MISS;

		InteractionHand hand = InteractionHand.MAIN_HAND;
		ItemStack stack = player.getMainHandItem();
		if(!isThrowable(stack))
		{
			hand = InteractionHand.OFF_HAND;
			stack = player.getOffhandItem();

			if(!isThrowable(stack))
				return new Trajectory(path, type);
		}

		Item item = stack.getItem();
		double throwPower = getThrowPower(item);
		double gravity = getProjectileGravity(item);
		Fluid fluidHandling = getFluidHandling(item);

		double yaw = Math.toRadians(player.getYRot());
		double pitch = Math.toRadians(player.getXRot());

		Vec3 arrowPos = EntityUtils.getLerpedPos(player, partialTicks)
			.add(getHandOffset(hand, yaw));
		Vec3 arrowMotion = getStartingMotion(yaw, pitch, throwPower);

		for(int i = 0; i < 1000; i++)
		{
			path.add(arrowPos);
			arrowPos = arrowPos.add(arrowMotion.scale(0.1));
			arrowMotion = arrowMotion.scale(0.999);
			arrowMotion = arrowMotion.add(0, -gravity * 0.1, 0);

			Vec3 lastPos = path.size() > 1 ? path.get(path.size() - 2)
				: arrowPos.subtract(arrowMotion.scale(0.1));

			BlockHitResult bResult =
				BlockUtils.raycast(lastPos, arrowPos, fluidHandling);
			if(bResult.getType() != HitResult.Type.MISS)
			{
				type = HitResult.Type.BLOCK;
				path.set(path.size() - 1, bResult.getLocation());
				break;
			}

			AABB box = new AABB(lastPos, arrowPos);
			Predicate<Entity> predicate =
				e -> !e.isSpectator() && e.isPickable();
			EntityHitResult eResult = ProjectileUtil.getEntityHitResult(player,
				lastPos, arrowPos, box, predicate, 64 * 64);

			if(eResult != null && eResult.getType() != HitResult.Type.MISS)
			{
				type = HitResult.Type.ENTITY;
				path.set(path.size() - 1, eResult.getLocation());
				break;
			}
		}

		return new Trajectory(path, type);
	}

	private boolean isThrowable(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;

		Item item = stack.getItem();
		return item instanceof ProjectileWeaponItem
			|| item instanceof SnowballItem || item instanceof EggItem
			|| item instanceof EnderpearlItem
			|| item instanceof ThrowablePotionItem
			|| item instanceof FishingRodItem || item instanceof TridentItem;
	}

	private double getThrowPower(Item item)
	{
		if(!(item instanceof ProjectileWeaponItem))
			return 1.5;

		float bowPower =
			(72000 - NovaClient.MC.player.getUseItemRemainingTicks()) / 20F;
		bowPower = bowPower * bowPower + bowPower * 2F;

		if(bowPower > 3 || bowPower <= 0.3F)
			bowPower = 3;

		return bowPower;
	}

	private double getProjectileGravity(Item item)
	{
		if(item instanceof ProjectileWeaponItem)
			return 0.05;

		if(item instanceof ThrowablePotionItem)
			return 0.4;

		if(item instanceof FishingRodItem)
			return 0.15;

		if(item instanceof TridentItem)
			return 0.015;

		return 0.03;
	}

	private Fluid getFluidHandling(Item item)
	{
		return item instanceof FishingRodItem ? Fluid.ANY : Fluid.NONE;
	}

	private Vec3 getHandOffset(InteractionHand hand, double yaw)
	{
		HumanoidArm mainArm = NovaClient.MC.options.mainHand().get();

		boolean rightSide = mainArm == HumanoidArm.RIGHT
			&& hand == InteractionHand.MAIN_HAND || mainArm == HumanoidArm.LEFT
				&& hand == InteractionHand.OFF_HAND;

		double sideMultiplier = rightSide ? -1 : 1;
		return new Vec3(Math.cos(yaw) * 0.16 * sideMultiplier,
			NovaClient.MC.player.getEyeHeight() - 0.1,
			Math.sin(yaw) * 0.16 * sideMultiplier);
	}

	private Vec3 getStartingMotion(double yaw, double pitch, double throwPower)
	{
		double cosOfPitch = Math.cos(pitch);

		return new Vec3(-Math.sin(yaw) * cosOfPitch, -Math.sin(pitch),
			Math.cos(yaw) * cosOfPitch).normalize().scale(throwPower);
	}

	private ColorSetting getColor(Trajectory trajectory)
	{
		return switch(trajectory.type())
		{
			case MISS -> missColor;
			case ENTITY -> entityHitColor;
			case BLOCK -> blockHitColor;
		};
	}

	private record Trajectory(List<Vec3> path, HitResult.Type type)
	{
		public boolean isEmpty()
		{
			return path.isEmpty();
		}

		public AABB getEndBox()
		{
			Vec3 end = path.get(path.size() - 1);
			return new AABB(end.subtract(0.5), end.add(0.5));
		}
	}
}
