/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.damage;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.tags.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "handleDamageEvent", at = @At("RETURN"))
	private void onDamageEvent(ClientboundDamageEventPacket packet, CallbackInfo ci) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		if (packet.entityId() == player.getId()) {
			playIncomingDamageRumble();
		} else if (packet.sourceCauseId() == player.getId()
				&& packet.sourceDirectId() == player.getId()
				&& packet.sourceType().is(DamageTypeTags.IS_PLAYER_ATTACK)) {
			playOutgoingDamageRumble();
		}
	}

	/// On legacy protocol servers such as Hypixel PvP gamemodes,
	/// ClientboundDamageEventPacket is not sent, so we instead rely on the hurt animation packet
	/// for such cases.
	@Inject(
			method = "handleHurtAnimation",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;animateHurt(F)V")
	)
	private void onHurtAnimation(ClientboundHurtAnimationPacket packet, CallbackInfo ci) {
		LocalPlayer player = Minecraft.getInstance().player;
		// if vanilla sent a damage event first, then hurtTime would have been set to 10
		if (player != null && packet.id() == player.getId() && player.hurtTime < 10) {
			playIncomingDamageRumble();
		}
	}

	@Unique private void playIncomingDamageRumble() {
		ControlifyApi.get().playRumbleEffect(
				RumbleSource.PLAYER,
				BasicRumbleEffect.constant(0.8f, 0.5f, 5)
		);
	}

	@Unique private void playOutgoingDamageRumble() {
		ControlifyApi.get().playRumbleEffect(
				RumbleSource.INTERACTION,
				BasicRumbleEffect.seq(
						BasicRumbleEffect.constant(0.18f, 0.4f, 1),
						BasicRumbleEffect.constant(0.06f, 0.16f, 1)
				)
		);
	}
}
