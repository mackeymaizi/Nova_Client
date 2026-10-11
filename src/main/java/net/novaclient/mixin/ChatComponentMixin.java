/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.misc.BetterChatModule;
import net.novaclient.module.modules.render.NameProtectModule;

/**
 * Applies BetterChat's anti-spam and NameProtect's name replacement to
 * incoming chat messages.
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin
{
	@ModifyVariable(method = "addServerSystemMessage",
		at = @At("HEAD"), argsOnly = true)
	private Component nova_protectServerSystem(Component message)
	{
		return protect(message);
	}

	@ModifyVariable(method = "addPlayerMessage",
		at = @At("HEAD"), argsOnly = true)
	private Component nova_protectPlayerMessage(Component message)
	{
		return protect(message);
	}

	@Inject(method = "addServerSystemMessage", at = @At("HEAD"),
		cancellable = true)
	private void nova_spamCheckServer(Component message, CallbackInfo ci)
	{
		if(isSpam(message))
			ci.cancel();
	}

	@Inject(method = "addPlayerMessage", at = @At("HEAD"),
		cancellable = true)
	private void nova_spamCheckPlayer(Component message,
		net.minecraft.network.chat.MessageSignature signature,
		GuiMessageTag tag, CallbackInfo ci)
	{
		if(isSpam(message))
			ci.cancel();
	}

	private boolean isSpam(Component message)
	{
		if(NovaClient.INSTANCE == null)
			return false;

		BetterChatModule betterChat = ModuleManager.get(BetterChatModule.class);

		return betterChat != null && betterChat.isSpam(message.getString());
	}

	private Component protect(Component message)
	{
		if(NovaClient.INSTANCE == null || message == null)
			return message;

		Component result = message;

		// BetterChat: timestamps, coords protection and the regex filter.
		BetterChatModule betterChat = ModuleManager.get(BetterChatModule.class);
		if(betterChat != null)
		{
			String rewritten = betterChat.rewrite(message.getString());

			if(rewritten != null
				&& !rewritten.equals(message.getString()))
			{
				MutableComponent replaced = Component.literal(rewritten);
				replaced.setStyle(message.getStyle());
				result = replaced;
			}
		}

		NameProtectModule nameProtect =
			ModuleManager.get(NameProtectModule.class);

		if(nameProtect == null || !nameProtect.inChat())
			return result;

		String original = result.getString();
		String replacedName = nameProtect.protect(original);

		if(original.equals(replacedName))
			return result;

		MutableComponent named = Component.literal(replacedName);
		named.setStyle(result.getStyle());
		return named;
	}
}
