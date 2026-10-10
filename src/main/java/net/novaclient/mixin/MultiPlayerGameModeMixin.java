/*
 * Copyright (c) 2026 Nova Client contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.novaclient.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.novaclient.event.EventBus;
import net.novaclient.event.events.BlockBreakingProgressEvent;
import net.novaclient.event.events.StopUsingItemEvent;
import net.novaclient.mixininterface.IMultiPlayerGameMode;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin implements IMultiPlayerGameMode
{
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(
		method = "continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;getId()I",
			ordinal = 0))
	private void nova_onContinueDestroyBlock(BlockPos pos, Direction direction,
		CallbackInfoReturnable<Boolean> cir)
	{
		EventBus.post(new BlockBreakingProgressEvent(pos));
	}

	@Inject(
		method = "releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V",
		at = @At("HEAD"))
	private void nova_onReleaseUsingItem(Player player, CallbackInfo ci)
	{
		EventBus.post(StopUsingItemEvent.INSTANCE);
	}

	@Override
	public void nova_windowClick_PICKUP(int slot)
	{
		handleInventoryMouseClick(0, slot, 0, ClickType.PICKUP, minecraft.player);
	}

	@Override
	public void nova_windowClick_QUICK_MOVE(int slot)
	{
		handleInventoryMouseClick(0, slot, 0, ClickType.QUICK_MOVE,
			minecraft.player);
	}

	@Override
	public void nova_windowClick_THROW(int slot)
	{
		handleInventoryMouseClick(0, slot, 1, ClickType.THROW, minecraft.player);
	}

	@Override
	public void nova_windowClick_SWAP(int from, int to)
	{
		handleInventoryMouseClick(0, from, to, ClickType.SWAP,
			minecraft.player);
	}

	@Override
	public void nova_rightClickItem()
	{
		useItem(minecraft.player, InteractionHand.MAIN_HAND);
	}

	@Override
	public void nova_rightClickBlock(BlockPos pos, Direction side, Vec3 hitVec)
	{
		BlockHitResult hitResult = new BlockHitResult(hitVec, side, pos, false);
		InteractionHand hand = InteractionHand.MAIN_HAND;
		useItemOn(minecraft.player, hand, hitResult);
		useItem(minecraft.player, hand);
	}

	@Override
	public void nova_sendPlayerActionC2SPacket(Action action, BlockPos blockPos,
		Direction direction)
	{
		startPrediction(minecraft.level,
			i -> new ServerboundPlayerActionPacket(action, blockPos, direction,
				i));
	}

	@Override
	public void nova_sendPlayerInteractBlockPacket(InteractionHand hand,
		BlockHitResult blockHitResult)
	{
		startPrediction(minecraft.level,
			i -> new ServerboundUseItemOnPacket(hand, blockHitResult, i));
	}

	@Shadow
	private void startPrediction(ClientLevel level,
		PredictiveAction packetCreator)
	{}

	@Shadow
	public abstract InteractionResult useItemOn(LocalPlayer player,
		InteractionHand hand, BlockHitResult hitResult);

	@Shadow
	public abstract InteractionResult useItem(Player player,
		InteractionHand hand);

	@Shadow
	public abstract void handleInventoryMouseClick(int syncId, int slotId,
		int button, ClickType actionType, Player player);
}
