/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.server.packets;

import dev.isxander.controlify.api.controller.rumble.RumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleState;
import dev.isxander.controlify.impl.controller.rumble.RumbleSource;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.Easings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public record OriginVibrationPacket(Vector3f origin, float effectRange, int duration, RumbleState state, RumbleSource source) {
	public static final Identifier CHANNEL = CUtil.rl("vibrate_from_origin");

	public static final StreamCodec<FriendlyByteBuf, OriginVibrationPacket> CODEC = StreamCodec.of(
		(buf, packet) -> {
			buf.writeVector3f(packet.origin());
			buf.writeFloat(packet.effectRange());
			buf.writeVarInt(packet.duration());
			buf.writeInt(RumbleState.packToInt(packet.state()));
			buf.writeIdentifier(packet.source().id());
		},
		buf -> new OriginVibrationPacket(
			buf.readVector3f(),
			buf.readFloat(),
			buf.readVarInt(),
			RumbleState.unpackFromInt(buf.readInt()),
			new RumbleSource(buf.readIdentifier())
		)
	);

	public RumbleEffect createEffect() {
		var originVec3 = new Vec3(origin);
		return RumbleEffect.continuous()
				.constant(state.strong(), state.weak())
				.inWorld(() -> originVec3, 0, 1, effectRange, Easings.toFloat(Easings::easeInSine))
				.timeout(duration);
	}
}
