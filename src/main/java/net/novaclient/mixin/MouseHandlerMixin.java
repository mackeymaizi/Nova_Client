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

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.novaclient.event.EventBus;
import net.novaclient.event.events.MouseButtonEvent;
import net.novaclient.event.events.MouseScrollEvent;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin
{
	@Inject(
		method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V",
		at = @At("HEAD"))
	private void nova_onButton(long windowHandle, MouseButtonInfo info,
		int action, CallbackInfo ci)
	{
		EventBus.post(
			new MouseButtonEvent(info.button(), action, info.modifiers()));
	}

	@Inject(method = "onScroll(JDD)V", at = @At("RETURN"))
	private void nova_onScroll(long window, double horizontal, double vertical,
		CallbackInfo ci)
	{
		EventBus.post(new MouseScrollEvent(horizontal, vertical));
	}
}
