/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import net.novaclient.command.Command;

/** {@code .damage <amount>} - damages yourself using fall damage. */
public final class DamageCommand extends Command
{
	private static final SimpleCommandExceptionType INVULNERABLE =
		new SimpleCommandExceptionType(
			Component.literal("You are invulnerable."));

	public DamageCommand()
	{
		super("damage", "Damages self.", "dmg");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(argument("damage",
			IntegerArgumentType.integer(1, 7)).executes(ctx -> {
				int amount = IntegerArgumentType.getInteger(ctx, "damage");

				if(mc().player.getAbilities().invulnerable)
					throw INVULNERABLE.create();

				damagePlayer(amount);
				return SINGLE_SUCCESS;
			}));
	}

	private void damagePlayer(int amount)
	{
		Vec3 pos = mc().player.position();

		for(int i = 0; i < 80; i++)
		{
			sendPositionPacket(pos.x, pos.y + amount + 2.1, pos.z, false);
			sendPositionPacket(pos.x, pos.y + 0.05, pos.z, false);
		}

		sendPositionPacket(pos.x, pos.y, pos.z, true);
	}

	private void sendPositionPacket(double x, double y, double z,
		boolean onGround)
	{
		mc().player.connection.send(new ServerboundMovePlayerPacket.Pos(x, y, z,
			onGround, mc().player.horizontalCollision));
	}
}
