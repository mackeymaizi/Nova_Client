/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.command.commands;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.novaclient.command.Command;
import net.novaclient.event.EventBus;
import net.novaclient.event.events.TickEvent;

/** {@code .input <key> [ticks]} - simulates keyboard input. */
public final class InputCommand extends Command
{
	private static final List<KeypressHandler> ACTIVE = new ArrayList<>();

	public InputCommand()
	{
		super("input", "Simulates keyboard input.");
	}

	@Override
	public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder)
	{
		addHold(builder, () -> mc().options.keyUp, "forwards");
		addHold(builder, () -> mc().options.keyDown, "backwards");
		addHold(builder, () -> mc().options.keyLeft, "left");
		addHold(builder, () -> mc().options.keyRight, "right");
		addHold(builder, () -> mc().options.keyJump, "jump");
		addHold(builder, () -> mc().options.keyShift, "sneak");
		addHold(builder, () -> mc().options.keySprint, "sprint");
		addHold(builder, () -> mc().options.keyUse, "use");
		addHold(builder, () -> mc().options.keyAttack, "attack");

		builder.then(literal("clear").executes(ctx -> {
			clear();
			info("Cleared all input handlers.");
			return SINGLE_SUCCESS;
		}));
	}

	private void addHold(LiteralArgumentBuilder<ClientSuggestionProvider> builder,
		java.util.function.Supplier<KeyMapping> key, String name)
	{
		builder.then(literal(name)
			.executes(ctx -> hold(key.get(), 1))
			.then(argument("ticks", IntegerArgumentType.integer(1))
				.executes(ctx -> hold(key.get(),
					IntegerArgumentType.getInteger(ctx, "ticks")))));
	}

	private int hold(KeyMapping key, int ticks)
	{
		ACTIVE.add(new KeypressHandler(key, ticks));
		return SINGLE_SUCCESS;
	}

	private static void clear()
	{
		for(KeypressHandler handler : ACTIVE)
			handler.stop();

		ACTIVE.clear();
	}

	private static final class KeypressHandler
	{
		private final KeyMapping key;
		private int ticks;

		KeypressHandler(KeyMapping key, int ticks)
		{
			this.key = key;
			this.ticks = ticks;
			EventBus.subscribe(TickEvent.class, this, this::onTick);
		}

		private void onTick(TickEvent event)
		{
			if(ticks-- > 0)
				key.setDown(true);
			else
			{
				key.setDown(false);
				stop();
				ACTIVE.remove(this);
			}
		}

		void stop()
		{
			key.setDown(false);
			EventBus.unsubscribe(this);
		}
	}
}
