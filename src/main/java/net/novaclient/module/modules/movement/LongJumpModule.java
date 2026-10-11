/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;

/**
 * Ported from Meteor's LongJump. Gives you a big forward leap when you jump.
 */
public final class LongJumpModule extends Module
{
	private final DoubleSetting boost = new DoubleSetting("Boost",
		"How strong the jump boost is.", 4.5, 0.1, 15, 0.1);

	private final DoubleSetting vertical = new DoubleSetting("Vertical",
		"Extra upward momentum.", 0.4, 0, 2, 0.05);

	private final BoolSetting autoJump = new BoolSetting("Auto jump",
		"Automatically jumps when on the ground.", false);

	private final DoubleSetting cooldown = new DoubleSetting("Cooldown",
		"Ticks to wait between jumps.", 10, 0, 100, 1);

	private final BoolSetting onlyOnGround = new BoolSetting("Only on ground",
		"Only boost while you are standing on the ground.", true);

	private final BoolSetting autoDisable = new BoolSetting("Auto disable",
		"Turns the module off after a single jump.", false);

	private final BoolSetting disableOnRubberband = new BoolSetting(
		"Disable on rubberband",
		"Turns the module off when the server teleports you back.", true);

	private double lastY;
	private int timer;

	public LongJumpModule()
	{
		super("LongJump", "Lets you jump much further than normal.",
			ModuleCategory.MOVEMENT);
		displayName("远跳");

		addSetting(boost);
		addSetting(vertical);
		addSetting(autoJump);
		addSetting(cooldown);
		addSetting(onlyOnGround);
		addSetting(autoDisable);
		addSetting(disableOnRubberband);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	@Override
	public void onEnable()
	{
		timer = 0;
		lastY = 0;
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player == null)
			return;

		// Rubberband detection: the server teleported us back a long way.
		if(disableOnRubberband.isChecked() && lastY != 0
			&& Math.abs(player.getY() - lastY) > 3)
		{
			setEnabled(false);
			return;
		}

		lastY = player.getY();

		if(timer > 0)
		{
			timer--;
			return;
		}

		boolean jumpKey = NovaClient.MC.options.keyJump.isDown();

		if(autoJump.isChecked() && player.onGround())
			player.jumpFromGround();

		if(!jumpKey && !autoJump.isChecked())
			return;

		if(onlyOnGround.isChecked() && !player.onGround() && !autoJump.isChecked())
			return;

		float yawRad = player.getYRot() * Mth.DEG_TO_RAD;
		Vec3 forward = new Vec3(-Mth.sin(yawRad), 0, Mth.cos(yawRad));

		player.addDeltaMovement(forward.scale(boost.get()));
		player.addDeltaMovement(new Vec3(0, vertical.get(), 0));

		timer = cooldown.get().intValue();

		if(autoDisable.isChecked())
			setEnabled(false);
	}
}
