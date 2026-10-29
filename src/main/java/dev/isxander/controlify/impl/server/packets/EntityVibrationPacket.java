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
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public record EntityVibrationPacket(int entityId, float range, int duration, RumbleState state, RumbleSource source) {
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
			new RumbleSource(buf.readIdentifier())
		)
	);

	public RumbleEffect createEffect() {
		Entity entity = Minecraft.getInstance().level.getEntity(entityId);
		return RumbleEffect.continuous()
				.constant(state.strong(), state.weak())
				.inWorld(entity::position, 0, 1, range, Easings.toFloat(Easings::easeInSine))
				.timeout(duration);
	}
}
