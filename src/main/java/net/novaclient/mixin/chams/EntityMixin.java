/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin.chams;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.novaclient.NovaClient;
import net.novaclient.module.ModuleManager;
import net.novaclient.module.modules.render.ChamsModule;

/** Colours the glow outline that Chams applies to entities. */
@Mixin(Entity.class)
public abstract class EntityMixin
{
	@Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
	private void nova_getChamsColor(CallbackInfoReturnable<Integer> cir)
	{
		if(NovaClient.INSTANCE == null)
			return;

		ChamsModule chams = ModuleManager.get(ChamsModule.class);

		if(chams == null || !chams.isEnabled())
			return;

		Entity self = (Entity)(Object)this;

		if(self instanceof Player && chams.outlinesPlayers())
			cir.setReturnValue(chams.getOutlineColor(self) & 0xFFFFFF);
		else if(self instanceof Mob && chams.outlinesMobs())
			cir.setReturnValue(chams.getOutlineColor(self) & 0xFFFFFF);
	}
}
