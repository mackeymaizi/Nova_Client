/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.novaclient.command.Command;

/** {@code .rotation set|add <pitch> [yaw]} - modifies your rotation. */
public final class RotationCommand extends Command
{
	public RotationCommand()
	{
		super("rotation", "Modifies your rotation.", "rot");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		builder.then(literal("set")
			.then(argument("pitch", FloatArgumentType.floatArg(-90, 90))
				.executes(ctx -> {
					mc().player.setXRot(ctx.getArgument("pitch", Float.class));
					return SINGLE_SUCCESS;
				})
				.then(argument("yaw", FloatArgumentType.floatArg(-180, 180))
					.executes(ctx -> {
						mc().player
							.setXRot(ctx.getArgument("pitch", Float.class));
						mc().player
							.setYRot(ctx.getArgument("yaw", Float.class));
						return SINGLE_SUCCESS;
					}))))
			.then(literal("add")
				.then(argument("pitch", FloatArgumentType.floatArg(-90, 90))
					.executes(ctx -> {
						float pitch = mc().player.getXRot()
							+ ctx.getArgument("pitch", Float.class);
						mc().player.setXRot(
							pitch >= 0 ? Math.min(pitch, 90)
								: Math.max(pitch, -90));
						return SINGLE_SUCCESS;
					})
					.then(argument("yaw",
						FloatArgumentType.floatArg(-180, 180))
						.executes(ctx -> {
							float pitch = mc().player.getXRot()
								+ ctx.getArgument("pitch", Float.class);
							mc().player.setXRot(
								pitch >= 0 ? Math.min(pitch, 90)
									: Math.max(pitch, -90));

							float yaw = mc().player.getYRot()
								+ ctx.getArgument("yaw", Float.class);
							mc().player.setYRot(Mth.wrapDegrees(yaw));
							return SINGLE_SUCCESS;
						}))))
			.then(literal("face")
				.then(literal("north").executes(ctx -> face(Direction.NORTH)))
				.then(literal("south").executes(ctx -> face(Direction.SOUTH)))
				.then(literal("east").executes(ctx -> face(Direction.EAST)))
				.then(literal("west").executes(ctx -> face(Direction.WEST)))
				.then(literal("up").executes(ctx -> facePitch(-90)))
				.then(literal("down").executes(ctx -> facePitch(90))));
	}

	private int face(Direction direction)
	{
		mc().player.setXRot(0);
		mc().player.setYRot(direction.toYRot());
		return SINGLE_SUCCESS;
	}

	private int facePitch(float pitch)
	{
		mc().player.setXRot(pitch);
		return SINGLE_SUCCESS;
	}
}
