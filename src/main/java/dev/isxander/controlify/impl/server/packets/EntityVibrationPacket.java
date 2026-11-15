/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.server.packets;

import dev.isxander.controlify.api.feedback.FeedbackEffect;
import dev.isxander.controlify.impl.controller.rumble.RumbleState;
import dev.isxander.controlify.impl.feedback.FeedbackSource;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public record EntityVibrationPacket(int entityId, float range, int duration, RumbleState state, FeedbackSource source) {
	public static final Identifier CHANNEL = CUtil.rl("vibrate_from_entity");

	public static final StreamCodec<FriendlyByteBuf, EntityVibrationPacket> CODEC = StreamCodec.of(
		(buf, packet) -> {
			buf.writeInt(packet.entityId());
			buf.writeFloat(packet.range());
			buf.writeInt(packet.duration());
			buf.writeInt(RumbleState.packToInt(packet.state()));
			buf.writeIdentifier(packet.source().id());
		},
		buf -> new EntityVibrationPacket(
			buf.readInt(),
			buf.readFloat(),
			buf.readInt(),
			RumbleState.unpackFromInt(buf.readInt()),
			new FeedbackSource(buf.readIdentifier())
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

	/// Where the vibration comes from, following the entity as it moves.
	public Supplier<Vec3> position() {
		Entity entity = Minecraft.getInstance().level.getEntity(entityId);
		Vec3 fallback = Minecraft.getInstance().getCameraEntity() != null
				? Minecraft.getInstance().getCameraEntity().position()
				: Vec3.ZERO;
		return () -> entity != null ? entity.position() : fallback;
	}
}
