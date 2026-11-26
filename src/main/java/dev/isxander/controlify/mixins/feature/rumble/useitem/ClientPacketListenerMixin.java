/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.useitem;

import dev.isxander.controlify.impl.feedback.BuiltinFeedback;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "handleRespawn", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;", opcode = Opcodes.PUTFIELD))
	private void clearUseItemRumble(ClientboundRespawnPacket packet, CallbackInfo ci) {
		BuiltinFeedback.USE_ITEM.release();
	}

}
