/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.Items;
import net.novaclient.NovaClient;
import net.novaclient.event.events.UpdateEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;

/** Ported from Wurst's NoFallHack. Prevents fall damage. */
public final class NoFallModule extends Module
{
	private final BoolSetting allowElytra = new BoolSetting("Allow elytra",
		"Keeps working while flying with an elytra.", false);

	private final BoolSetting pauseForMace = new BoolSetting("Pause for mace",
		"Pauses while holding a mace, so smash attacks still work.", true);

	private final DoubleSetting minFallDistance = new DoubleSetting(
		"Min fall distance", "Only activates after falling this far.",
		1, 0, 10, 0.1);

	private final DoubleSetting minElytraFallDistance = new DoubleSetting(
		"Min elytra fall distance", "Same, but while flying with an elytra.",
		2, 0, 10, 0.1);

	public NoFallModule()
	{
		super("NoFall", "Prevents you from taking fall damage.",
			ModuleCategory.MOVEMENT);
		displayName("防摔伤");

		addSetting(allowElytra);
		addSetting(pauseForMace);
		addSetting(minFallDistance);
		addSetting(minElytraFallDistance);

		subscribe(UpdateEvent.class, e -> onUpdate());
	}

	private void onUpdate()
	{
		var player = NovaClient.MC.player;
		if(player == null || isPaused())
			return;

		player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true,
			player.horizontalCollision));
	}

	private boolean isPaused()
	{
		var player = NovaClient.MC.player;

		// No fall damage in creative.
		if(player.getAbilities().invulnerable)
			return true;

		boolean fallFlying = player.isFallFlying();

		if(fallFlying && !allowElytra.isChecked())
			return true;

		if(pauseForMace.isChecked() && player.getMainHandItem().is(Items.MACE))
			return true;

		double min = fallFlying ? minElytraFallDistance.get()
			: minFallDistance.get();

		return player.fallDistance <= min;
	}
}
