/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.autoreconnect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.misc.AutoReconnectModule;

/** Starts the AutoReconnect countdown when a disconnect screen appears. */
@Mixin(Gui.class)
public abstract class GuiMixin
{
	@Inject(method = "setScreen", at = @At("HEAD"))
	private void nova_onSetScreen(Screen screen, CallbackInfo ci)
	{
		if(NovaClient.INSTANCE == null
			|| !(screen instanceof DisconnectedScreen))
			return;

		AutoReconnectModule autoReconnect =
			ModuleManager.get(AutoReconnectModule.class);

		if(autoReconnect != null)
			autoReconnect.scheduleReconnect();
	}
}
