/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.server;

import dev.isxander.controlify.impl.server.packets.FeedbackEffectPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FeedbackEffectPacketTest {
	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("controlify_test", path);
	}

	private static FeedbackEffectPacket roundTrip(FeedbackEffectPacket packet) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		FeedbackEffectPacket.CODEC.encode(buf, packet);
		return FeedbackEffectPacket.CODEC.decode(buf);
	}

	@Test
	void roundTripsEveryField() {
		var packet = new FeedbackEffectPacket(
				id("rumble"),
				new FeedbackEffectPacket.Origin.Position(new Vec3(1, 2, 3)),
				List.of(id("a"), id("b")),
				Map.of("power", 0.5f)
		);
		assertEquals(packet, roundTrip(packet));
	}

	@Test
	void roundTripsEntityOriginAndNoOrigin() {
		var entity = new FeedbackEffectPacket(id("e"), new FeedbackEffectPacket.Origin.EntityOrigin(42), List.of(), Map.of());
		assertEquals(entity, roundTrip(entity));

		var none = new FeedbackEffectPacket(id("e"), null, List.of(), Map.of());
		assertNull(roundTrip(none).origin());
	}

	@Test
	void rejectsTooManyGroups() {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeIdentifier(id("e"));
		buf.writeByte(0);
		buf.writeVarInt(Integer.MAX_VALUE);
		assertThrows(IllegalArgumentException.class, () -> FeedbackEffectPacket.CODEC.decode(buf));
	}
}
