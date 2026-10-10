/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.novaclient.NovaClient;
import net.novaclient.command.CommandManager;
import net.novaclient.event.EventBus;
import net.novaclient.event.events.ChatSendEvent;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.misc.InfiniChatModule;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen
{
	@Shadow
	protected EditBox input;

	private ChatScreenMixin(Component title)
	{
		super(title);
	}

	@Inject(method = "init()V", at = @At("TAIL"))
	protected void nova_onInit(CallbackInfo ci)
	{
		if(NovaClient.INSTANCE == null)
			return;

		if(ModuleManager.get(InfiniChatModule.class).isEnabled())
			input.setMaxLength(Integer.MAX_VALUE);
	}

	@Inject(method = "handleChatInput(Ljava/lang/String;Z)V",
		at = @At("HEAD"),
		cancellable = true)
	public void nova_onHandleChatInput(String message, boolean addToHistory,
		CallbackInfo ci)
	{
		if((message = normalizeChatMessage(message)).isEmpty())
			return;

		// Nova commands take priority over chat.
		if(CommandManager.isCommand(message))
		{
			if(addToHistory)
				minecraft.gui.hud.getChat().addRecentChat(message);

			CommandManager.onChatInput(message);
			ci.cancel();
			return;
		}

		ChatSendEvent event = new ChatSendEvent(message);
		EventBus.post(event);

		boolean cancelled = event.isCancelled();
		if(!cancelled && event.message().equals(message))
			return;

		ci.cancel();

		String newMessage = event.message();
		if(addToHistory)
			minecraft.gui.hud.getChat().addRecentChat(newMessage);

		if(cancelled || minecraft.player == null)
			return;

		if(newMessage.startsWith("/"))
			minecraft.player.connection
				.sendCommand(newMessage.substring(1));
		else
			minecraft.player.connection.sendChat(newMessage);
	}

	@Shadow
	public abstract String normalizeChatMessage(String chatText);
}
