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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
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
	}
}
