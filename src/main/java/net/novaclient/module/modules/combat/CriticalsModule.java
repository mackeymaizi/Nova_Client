/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.combat;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MaceItem;
import net.novaclient.NovaClient;
import net.novaclient.event.events.PacketSendEvent;
import net.novaclient.event.events.TickEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.module.ModuleManager;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.EnumSetting;
import net.novaclient.util.PacketUtils;

/** Ported from Meteor's Criticals. */
public final class CriticalsModule extends Module
{
	private final EnumSetting<Mode> mode = new EnumSetting<>("Mode",
		"The mode on how Criticals will function.", Mode.values(),
		Mode.Packet);

	private final BoolSetting ka = new BoolSetting("Only KillAura",
		"Only performs crits when attacking the KillAura target.", false);

	private final BoolSetting mace = new BoolSetting("Smash attack",
		"Will always perform smash attacks when using a mace.", true);

	private final DoubleSetting extraHeight = new DoubleSetting(
		"Additional height",
		"The amount of additional height to spoof. More height means more"
			+ " damage.",
		0, 0, 100, 0.1);

	private ServerboundInteractPacket attackPacket;
	private ServerboundSwingPacket swingPacket;
	private boolean sendPackets;
	private int sendTimer;
	private double lastY;
	private boolean waitingForPeak;

	public CriticalsModule()
	{
		super("Criticals", "Performs critical attacks when you hit your target.",
			ModuleCategory.COMBAT);
		displayName("暴击");

		addSetting(mode);
		addSetting(ka);
		addSetting(mace);
		addSetting(extraHeight);

		subscribe(PacketSendEvent.class, this::onSendPacket);
		subscribe(TickEvent.class, this::onTick);
	}

	@Override
	public void onEnable()
	{
		attackPacket = null;
		swingPacket = null;
		sendPackets = false;
		sendTimer = 0;
		lastY = 0;
		waitingForPeak = false;
	}

	private void onSendPacket(PacketSendEvent event)
	{
		var mc = NovaClient.MC;

		if(event.packet() instanceof ServerboundInteractPacket packet
			&& PacketUtils.isAttack(packet))
		{
			if(mace.isChecked()
				&& mc.player.getMainHandItem().getItem() instanceof MaceItem)
			{
				if(mc.player.isFallFlying())
					return;

				sendPacket(0);
				sendPacket(1.501 + extraHeight.get());
				sendPacket(0);
				return;
			}

			if(skipCrit())
				return;

			Entity entity = mc.crosshairPickEntity;
			KillAuraModule killAura =
				ModuleManager.get(KillAuraModule.class);

			if(!(entity instanceof LivingEntity)
				|| (ka.isChecked() && killAura != null
					&& entity != killAura.getTarget()))
				return;

			switch(mode.get())
			{
				case Packet -> {
					sendPacket(0.0625);
					sendPacket(0);
				}
				case UpdatedNCP -> {
					sendPacket(0.0000008);
					sendPacket(0);
				}
				case OldNCP -> {
					sendPacket(0.11);
					sendPacket(0.1100013579);
					sendPacket(0.0000013579);
				}
				case Jump, MiniJump -> {
					if(!sendPackets)
					{
						sendPackets = true;
						attackPacket = packet;

						if(mode.get() == Mode.MiniJump)
							mc.player.setDeltaMovement(mc.player.getDeltaMovement().x,
								0.25, mc.player.getDeltaMovement().z);

						sendTimer = mode.get() == Mode.Jump ? 0 : 4;
						event.cancel();
					}
				}
				case None -> {
				}
			}
		}else if(event.packet() instanceof ServerboundSwingPacket swing
			&& mode.get() != Mode.Packet && mode.get() != Mode.None)
		{
			if(skipCrit())
				return;

			if(sendPackets && swingPacket == null)
			{
				swingPacket = swing;
				event.cancel();
			}
		}
	}

	private void onTick(TickEvent event)
	{
		if(!event.isPre() || !sendPackets)
			return;

		if(mode.get() == Mode.Jump && waitingForPeak)
		{
			double currentY = NovaClient.MC.player.getY();
			if(currentY <= lastY)
			{
				waitingForPeak = false;
				sendTimer = 0;
			}
			lastY = currentY;
			return;
		}

		if(sendTimer <= 0)
		{
			if(attackPacket == null || swingPacket == null)
			{
				sendPackets = false;
				return;
			}

			NovaClient.MC.getConnection().send(attackPacket);
			NovaClient.MC.getConnection().send(swingPacket);

			attackPacket = null;
			swingPacket = null;
			sendPackets = false;
		}else
		{
			sendTimer--;
		}
	}

	public void queueJumpCrit(ServerboundInteractPacket packet)
	{
		sendPackets = true;
		attackPacket = packet;
		NovaClient.MC.player.jumpFromGround();
		waitingForPeak = true;
		lastY = NovaClient.MC.player.getY();
	}

	private void sendPacket(double height)
	{
		var player = NovaClient.MC.player;

		ServerboundMovePlayerPacket packet = new ServerboundMovePlayerPacket.Pos(
			player.getX(), player.getY() + height, player.getZ(), false, false);

		player.connection.send(packet);
	}

	private boolean skipCrit()
	{
		var player = NovaClient.MC.player;

		if(mode.get() == Mode.Jump || mode.get() == Mode.MiniJump)
		{
			if(!player.onGround())
				return true;
		}

		return !player.onGround() || player.isInWater() || player.isInLava()
			|| player.onClimbable();
	}

	@Override
	public String getInfoString()
	{
		return mode.get().name();
	}

	public enum Mode
	{
		None, Packet, UpdatedNCP, OldNCP, Jump, MiniJump;
	}
}
