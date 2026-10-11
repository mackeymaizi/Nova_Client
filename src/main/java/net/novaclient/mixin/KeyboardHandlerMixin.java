/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.novaclient.NovaClient;
import net.novaclient.command.CommandManager;
import net.novaclient.event.EventBus;
import net.novaclient.event.events.KeyPressEvent;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin
{
	@Inject(method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V",
		at = @At("HEAD"))
	private void nova_onKeyPress(long windowHandle, int action, KeyEvent event,
		CallbackInfo ci)
	{
		EventBus.post(new KeyPressEvent(windowHandle, event.key(), action,
			event.modifiers()));

		nova$commandShortcut(action, event);
	}

	/**
	 * Pressing {@code .} while no screen is open opens chat with the Nova
	 * command prefix pre-filled - just like vanilla's {@code /} key.
	 */
	private void nova$commandShortcut(int action, KeyEvent event)
	{
		if(NovaClient.INSTANCE == null || NovaClient.MC == null)
			return;

		if(action != GLFW.GLFW_PRESS)
			return;

		if(event.key() != GLFW.GLFW_KEY_PERIOD)
			return;

		// Don't hijack the key while a screen (or chat) already has focus.
		if(NovaClient.MC.gui.screen() != null)
			return;

		NovaClient.MC.gui.setScreen(
			new ChatScreen(CommandManager.PREFIX, false));
	}
}
