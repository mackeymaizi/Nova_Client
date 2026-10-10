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
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.novaclient.command.Command;

/**
 * {@code .vclip <blocks>} - teleports you vertically. Uses the Paper
 * "packet spam" trick to go further than the vanilla limit.
 */
public final class VClipCommand extends Command
{
	public VClipCommand()
	{
		super("vclip", "Lets you clip through blocks vertically.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(argument("blocks", DoubleArgumentType.doubleArg())
			.executes(ctx -> {
				double blocks = ctx.getArgument("blocks", Double.class);

				int packetsRequired =
					(int)Math.ceil(Math.abs(blocks / 10));
				if(packetsRequired > 20)
					packetsRequired = 1;

				if(mc().player.isPassenger())
				{
					for(int i = 0; i < packetsRequired - 1; i++)
						mc().player.connection.send(
							ServerboundMoveVehiclePacket
								.fromEntity(mc().player.getVehicle()));

					var vehicle = mc().player.getVehicle();
					vehicle.setPos(vehicle.getX(), vehicle.getY() + blocks,
						vehicle.getZ());
					mc().player.connection.send(
						ServerboundMoveVehiclePacket.fromEntity(vehicle));
				}else
				{
					for(int i = 0; i < packetsRequired - 1; i++)
						mc().player.connection.send(
							new ServerboundMovePlayerPacket.StatusOnly(true,
								mc().player.horizontalCollision));

					mc().player.connection.send(
						new ServerboundMovePlayerPacket.Pos(mc().player.getX(),
							mc().player.getY() + blocks, mc().player.getZ(),
							true, mc().player.horizontalCollision));
					mc().player.setPos(mc().player.getX(),
						mc().player.getY() + blocks, mc().player.getZ());
				}

				return SINGLE_SUCCESS;
			}));
	}
}
