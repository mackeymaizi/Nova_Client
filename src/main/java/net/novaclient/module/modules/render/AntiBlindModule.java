/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.render;

import net.minecraft.world.effect.MobEffects;
import net.novaclient.event.events.MobEffectEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;

/** Ported from Wurst's AntiBlindHack. */
public final class AntiBlindModule extends Module
{
	public AntiBlindModule()
	{
		super("AntiBlind", "Removes the blindness and darkness effects.",
			ModuleCategory.RENDER);
		displayName("防失明");

		subscribe(MobEffectEvent.class, e -> {
			if(e.effect() == MobEffects.BLINDNESS
				|| e.effect() == MobEffects.DARKNESS)
				e.setInstance(null);
		});
	}
}
