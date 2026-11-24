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
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record VibrationPacket(FeedbackGroup group, RumbleState[] frames) {
	public static final Identifier CHANNEL = CUtil.rl("vibration");

	public static final StreamCodec<FriendlyByteBuf, VibrationPacket> CODEC = StreamCodec.of(
		(buf, packet) -> {
			buf.writeIdentifier(packet.group().id());
			buf.writeInt(packet.frames().length);
			for (RumbleState frame : packet.frames()) {
				buf.writeInt(RumbleState.packToInt(frame));
			}
		},
		buf -> {
			FeedbackGroup group = new FeedbackGroup(buf.readIdentifier());
			RumbleState[] frames = new RumbleState[buf.readInt()];
			for (int i = 0; i < frames.length; i++) {
				frames[i] = RumbleState.unpackFromInt(buf.readInt());
			}
			return new VibrationPacket(group, frames);
		}
	);

	/// Plays the frames back at 20 per second, as the server authored them.
	public FeedbackEffect<Object> createEffect() {
		return (playback, out) -> {
			int frame = (int) (playback.seconds() * 20f);
			if (frame >= frames.length || Minecraft.getInstance().level == null) {
				return false;
			}
			out.rumble(frames[frame].strong(), frames[frame].weak());
			return true;
		};
	}
}
