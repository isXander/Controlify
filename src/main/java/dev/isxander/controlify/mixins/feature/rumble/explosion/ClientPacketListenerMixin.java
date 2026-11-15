/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.explosion;

import dev.isxander.controlify.impl.feedback.BuiltinFeedback;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "handleExplosion", at = @At("RETURN"))
	private void onClientExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
		BuiltinFeedback.EXPLOSION.fireAt(packet.center(), new BuiltinFeedback.Explosion(Math.max(packet.radius(), 0f)));
	}
}
