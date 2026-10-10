/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.novaclient.command.CommandManager;

/**
 * Hooks Nova's Brigadier dispatcher into the vanilla command suggestion
 * engine, which is what gives {@code .} commands their Tab-completion.
 */
@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin
{
	@Shadow
	private ParseResults<ClientSuggestionProvider> currentParse;

	@Shadow
	@Final
	private EditBox input;

	@Shadow
	private CommandSuggestions.SuggestionsList suggestions;

	@Shadow
	private boolean keepSuggestions;

	@Shadow
	private CompletableFuture<Suggestions> pendingSuggestions;

	@Shadow
	protected abstract void updateUsageInfo();

	@Inject(method = "updateCommandInfo", at = @At(value = "INVOKE",
		target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false),
		cancellable = true)
	public void nova_onUpdateCommandInfo(CallbackInfo ci,
		@Local(type = StringReader.class) StringReader reader)
	{
		String prefix = CommandManager.PREFIX;
		int length = prefix.length();

		if(!reader.canRead(length)
			|| !reader.getString().startsWith(prefix, reader.getCursor()))
			return;

		reader.setCursor(reader.getCursor() + length);

		if(currentParse == null)
			currentParse = CommandManager.DISPATCHER.parse(reader,
				Minecraft.getInstance().getConnection()
					.getSuggestionsProvider());

		int cursor = input.getCursorPosition();
		if(cursor >= length
			&& (suggestions == null || !keepSuggestions))
		{
			pendingSuggestions = CommandManager.DISPATCHER
				.getCompletionSuggestions(currentParse, cursor);
			pendingSuggestions.thenAccept(result -> {
				if(pendingSuggestions.isDone())
					updateUsageInfo();
			});
		}

		ci.cancel();
	}
}
