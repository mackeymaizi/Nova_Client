/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.module.modules.movement;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.event.events.PacketReceiveEvent;
import net.novaclient.event.events.PacketSendEvent;
import net.novaclient.event.events.PreMotionEvent;
import net.novaclient.module.Module;
import net.novaclient.module.ModuleCategory;
import net.novaclient.setting.BoolSetting;
import net.novaclient.setting.DoubleSetting;
import net.novaclient.setting.IntSetting;

/**
 * Ported from Meteor Rejects' PacketFly.
 *
 * <p>
 * Instead of moving through the vanilla physics engine, this sends
 * {@link ServerboundMovePlayerPacket.Pos} packets with the position you want
 * to be at and swallows the vanilla ones. Servers that trust the client
 * (or only loosely validate movement) will accept the position, which lets
 * you fly — and, with the speed settings turned up, move absurdly fast.
 *
 * <p>
 * How fast you can actually go depends entirely on the server: the speed
 * settings only control how large the offset per packet is. A server with a
 * proper movement check will rubber-band you back regardless.
 */
public final class PacketFlyModule extends Module
{
	private final DoubleSetting horizontalSpeed = new DoubleSetting(
		"Horizontal speed", "Horizontal speed in blocks per second.", 5.2,
		0, 100000, 0.1);

	private final DoubleSetting verticalSpeed = new DoubleSetting(
		"Vertical speed", "Vertical speed in blocks per second.", 1.24,
		0, 100000, 0.01);

	private final IntSetting packetsPerTick = new IntSetting("Packets per tick",
		"Splits the per-tick move into this many packets."
			+ " 1 = one big jump (fastest, most obvious).",
		1, 1, 40, 1);

	private final BoolSetting setPos = new BoolSetting("Set position",
		"Teleports the client to the sent position."
			+ " Required for very high speeds, otherwise the client cannot"
			+ " keep up.",
		true);

	private final BoolSetting teleport = new BoolSetting("Teleport",
		"Sends a teleport-confirmation with every tick.", true);

	private final BoolSetting invalidPacket = new BoolSetting("Invalid packet",
		"Also sends an out-of-bounds packet, which some servers treat as a"
			+ " teleport and accept.",
		false);

	private final BoolSetting antiKick = new BoolSetting("Anti-kick",
		"Nudges you down every so often so the server does not kick you"
			+ " for standing still.",
		true);

	private final IntSetting downDelay = new IntSetting("Down delay",
		"Ticks between the anti-kick nudges while hovering.", 4, 1, 40, 1);

	private final IntSetting flyingDownDelay = new IntSetting(
		"Flying down delay",
		"Ticks between the anti-kick nudges while ascending.", 10, 1, 40, 1);

	private final BoolSetting setYaw = new BoolSetting("Set yaw",
		"Also sends your yaw in the movement packets.", true);

	private final BoolSetting setMove = new BoolSetting("Set move",
		"Also sends your movement input in the packets.", false);

	private final BoolSetting setId = new BoolSetting("Set ID",
		"Sends a fresh teleport id with each position packet.", true);

	/** Packets we sent ourselves; every other movement packet is dropped. */
	private final Set<Packet<?>> own = Collections
		.newSetFromMap(new IdentityHashMap<>());

	private int flightCounter;
	private int teleportId;

	public PacketFlyModule()
	{
		super("PacketFly",
			"Flies by sending movement packets instead of moving normally.",
			ModuleCategory.MOVEMENT);
		displayName("发包飞行");

		addSetting(horizontalSpeed);
		addSetting(verticalSpeed);
		addSetting(packetsPerTick);
		addSetting(setPos);
		addSetting(teleport);
		addSetting(invalidPacket);
		addSetting(antiKick);
		addSetting(downDelay);
		addSetting(flyingDownDelay);
		addSetting(setYaw);
		addSetting(setMove);
		addSetting(setId);

		subscribe(PreMotionEvent.class, e -> onPreMotion());
		subscribe(PacketSendEvent.class, this::onSend);
		subscribe(PacketReceiveEvent.class, this::onReceive);
	}

	@Override
	public void onEnable()
	{
		flightCounter = 0;
		teleportId = 0;
		own.clear();
	}

	@Override
	public void onDisable()
	{
		own.clear();
	}

	@Override
	public String getInfoString()
	{
		return String.format("%.1f", horizontalSpeed.get());
	}

	private void onPreMotion()
	{
		var mc = NovaClient.MC;
		var player = mc.player;

		if(player == null || mc.level == null || player.connection == null)
			return;

		var keys = player.input.keyPresses;
		boolean free = isFree();
		boolean hasMove = keys.forward() || keys.backward()
			|| keys.left() || keys.right();

		double vy;

		if(keys.jump() && (free || !hasMove))
		{
			vy = verticalSpeed.get() / 20.0;

			// While ascending the server is happy, so we only need the
			// occasional nudge.
			if(antiKick.isChecked() && !free && count(flyingDownDelay.get()))
				vy = -0.032;
		}
		else if(keys.shift())
		{
			vy = -verticalSpeed.get() / 20.0;
		}
		else
		{
			vy = 0;

			// Hovering in a block: drift down slowly so the server does
			// not decide we are standing still.
			if(antiKick.isChecked() && !free && count(downDelay.get()))
				vy = -0.04;
		}

		Vec3 horizontal = horizontalVelocity(horizontalSpeed.get());
		sendMotion(new Vec3(horizontal.x, vy, horizontal.z));
	}

	/**
	 * Sends the movement packet(s) for this tick.
	 *
	 * <p>
	 * When {@code Set position} is on we move the player ourselves and zero
	 * the velocity, so the physics engine does not add the same offset a
	 * second time on the next tick.
	 */
	private void sendMotion(Vec3 motion)
	{
		var player = NovaClient.MC.player;
		Vec3 from = new Vec3(player.getX(), player.getY(), player.getZ());
		Vec3 end = from.add(motion);

		int steps = Math.max(1, packetsPerTick.get());

		for(int i = 1; i <= steps; i++)
		{
			Vec3 to = from.add(motion.scale((double)i / steps));

			send(movePacket(to, player));

			if(invalidPacket.isChecked())
			{
				Vec3 bad = to.add(0, 1500, 0);
				send(movePacket(bad, player));
			}
		}

		if(teleport.isChecked())
			send(new ServerboundAcceptTeleportationPacket(setId.isChecked()
				? ++teleportId : teleportId));

		if(setPos.isChecked())
		{
			player.setPosRaw(end.x, end.y, end.z);
			player.resetFallDistance();
		}
		else
		{
			player.setDeltaMovement(motion);
		}
	}

	/** Builds the position packet, honouring the yaw/move settings. */
	private ServerboundMovePlayerPacket movePacket(Vec3 to,
		net.minecraft.client.player.LocalPlayer player)
	{
		boolean onGround = player.onGround();
		boolean collision = player.horizontalCollision;

		if(setYaw.isChecked())
			return new ServerboundMovePlayerPacket.PosRot(to.x, to.y, to.z,
				player.getYRot(), player.getXRot(), onGround, collision);

		return new ServerboundMovePlayerPacket.Pos(to.x, to.y, to.z, onGround,
			collision);
	}

	private void send(Packet<?> packet)
	{
		own.add(packet);
		NovaClient.MC.player.connection.send(packet);
	}

	private void onSend(PacketSendEvent event)
	{
		if(!(event.packet() instanceof ServerboundMovePlayerPacket packet))
			return;

		// Pure look packets are fine — only the position ones would fight
		// with what we are sending.
		if(!packet.hasPosition())
			return;

		if(!own.remove(event.packet()))
			event.cancel();
	}

	private void onReceive(PacketReceiveEvent event)
	{
		if(!(event.packet() instanceof ClientboundPlayerPositionPacket packet))
			return;

		// Keep accepting whatever teleport the server hands out, otherwise
		// it rubber-bands us forever.
		teleportId = packet.id();
	}

	/** True when the player is not clipping into any block. */
	private boolean isFree()
	{
		var player = NovaClient.MC.player;
		AABB box = player.getBoundingBox()
			.expandTowards(-0.0625, -0.0625, -0.0625);

		return !NovaClient.MC.level.getBlockCollisions(player, box)
			.iterator().hasNext();
	}

	/** Returns true (and resets) once every {@code ticks} calls. */
	private boolean count(int ticks)
	{
		if(++flightCounter < ticks)
			return false;

		flightCounter = 0;
		return true;
	}

	/** Input direction scaled to {@code bps / 20} blocks per tick. */
	private Vec3 horizontalVelocity(double bps)
	{
		var player = NovaClient.MC.player;
		var keys = player.input.keyPresses;

		float yaw = player.getYRot();
		Vec3 forward = Vec3.directionFromRotation(0, yaw);
		Vec3 right = Vec3.directionFromRotation(0, yaw + 90);

		double dx = 0;
		double dz = 0;

		if(keys.forward())
		{
			dx += forward.x;
			dz += forward.z;
		}

		if(keys.backward())
		{
			dx -= forward.x;
			dz -= forward.z;
		}

		if(keys.right())
		{
			dx += right.x;
			dz += right.z;
		}

		if(keys.left())
		{
			dx -= right.x;
			dz -= right.z;
		}

		double length = Math.sqrt(dx * dx + dz * dz);

		if(length < 1.0E-6)
			return Vec3.ZERO;

		double scale = bps / 20.0 / length;
		return new Vec3(dx * scale, 0, dz * scale);
	}
}
