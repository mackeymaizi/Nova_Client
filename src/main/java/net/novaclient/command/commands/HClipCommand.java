/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.novaclient.command.Command;

/** {@code .hclip <blocks>} - teleports you horizontally. */
public final class HClipCommand extends Command
{
	public HClipCommand()
	{
		super("hclip", "Lets you clip through blocks horizontally.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(argument("blocks", DoubleArgumentType.doubleArg())
			.executes(ctx -> {
				double blocks = ctx.getArgument("blocks", Double.class);
				Vec3 forward = Vec3
					.directionFromRotation(0, mc().player.getYRot())
					.normalize();

				if(mc().player.isPassenger())
				{
					Entity vehicle = mc().player.getVehicle();
					vehicle.setPos(vehicle.getX() + forward.x * blocks,
						vehicle.getY(), vehicle.getZ() + forward.z * blocks);
				}

				mc().player.setPos(mc().player.getX() + forward.x * blocks,
					mc().player.getY(), mc().player.getZ() + forward.z * blocks);

				return SINGLE_SUCCESS;
			}));
	}
}
