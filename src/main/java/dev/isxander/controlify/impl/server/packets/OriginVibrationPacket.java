/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.server.packets;

import dev.isxander.controlify.api.feedback.FeedbackEffect;
import dev.isxander.controlify.impl.controller.rumble.RumbleState;
import dev.isxander.controlify.impl.feedback.FeedbackGroup;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.function.Supplier;

public record OriginVibrationPacket(Vector3f origin, float effectRange, int duration, RumbleState state, FeedbackGroup group) {
	public static final Identifier CHANNEL = CUtil.rl("vibrate_from_origin");

	public static final StreamCodec<FriendlyByteBuf, OriginVibrationPacket> CODEC = StreamCodec.of(
		(buf, packet) -> {
			buf.writeVector3f(packet.origin());
			buf.writeFloat(packet.effectRange());
			buf.writeVarInt(packet.duration());
			buf.writeInt(RumbleState.packToInt(packet.state()));
			buf.writeIdentifier(packet.group().id());
		},
		buf -> new OriginVibrationPacket(
			buf.readVector3f(),
			buf.readFloat(),
			buf.readVarInt(),
			RumbleState.unpackFromInt(buf.readInt()),
			new FeedbackGroup(buf.readIdentifier())
		)
	);

	/// A constant vibration lasting `duration` ticks. The engine attenuates it by distance.
	public FeedbackEffect<Object> createEffect() {
		float seconds = duration / 20f;
		return (playback, out) -> {
			if (playback.seconds() >= seconds) {
				return false;
			}
			out.rumble(state.strong(), state.weak());
			return true;
		};
	}

	public Supplier<Vec3> position() {
		Vec3 originVec3 = new Vec3(origin);
		return () -> originVec3;
	}
}
