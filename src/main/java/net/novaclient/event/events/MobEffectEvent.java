package net.novaclient.event.events;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import net.novaclient.event.Event;

public final class MobEffectEvent extends Event
{
	private final Holder<MobEffect> effect;
	private MobEffectInstance instance;

	public MobEffectEvent(Holder<MobEffect> effect,
		MobEffectInstance instance)
	{
		this.effect = effect;
		this.instance = instance;
	}

	public Holder<MobEffect> effect()
	{
		return effect;
	}

	public MobEffectInstance instance()
	{
		return instance;
	}

	public void setInstance(MobEffectInstance instance)
	{
		this.instance = instance;
	}
}
