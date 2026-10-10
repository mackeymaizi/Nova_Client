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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.novaclient.NovaClient;
import net.novaclient.mixininterface.IKeyMapping;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements IKeyMapping
{
	@Shadow
	protected InputConstants.Key key;

	@Override
	@Unique
	public boolean nova_isActuallyDown()
	{
		int code = key.getValue();

		if(key.getType() == InputConstants.Type.MOUSE)
			return GLFW.glfwGetMouseButton(
				NovaClient.MC.getWindow().handle(), code) == 1;

		return InputConstants.isKeyDown(NovaClient.MC.getWindow(), code);
	}

	@Override
	@Unique
	public void nova_resetPressedState()
	{
		setDown(nova_isActuallyDown());
	}

	@Shadow
	public abstract void setDown(boolean pressed);
}
