/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.server.packets;

import dev.isxander.controlify.impl.feedback.FeedbackGroup;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/// Tells the client to play a feedback effect it already has, from a resource pack.
/// The effect itself is never sent: an id the client does not know plays nothing.
///
/// @param effect the id of the effect to play
/// @param origin where the effect is felt from, or null to play it everywhere
/// @param groups the groups the effect is mixed as, or empty to use the ones resource packs give it
/// @param params named values the effect can read with `scale`
public record FeedbackEffectPacket(
		Identifier effect,
		@Nullable Origin origin,
		List<Identifier> groups,
		Map<String, Float> params
) {
	public static final Identifier CHANNEL = CUtil.rl("play_feedback");

	/// Bounds on what a packet can ask the client to allocate.
	public static final int MAX_GROUPS = 16;
	public static final int MAX_PARAMS = 32;
	private static final int MAX_PARAM_NAME_LENGTH = 64;

	public sealed interface Origin {
		/// A fixed position in the world.
		record Position(Vec3 pos) implements Origin {
		}

		/// An entity, looked up by the client every frame so the effect follows it.
		record EntityOrigin(int entityId) implements Origin {
		}
	}

	public FeedbackEffectPacket {
		groups = List.copyOf(groups);
		params = Map.copyOf(params);
	}

	public static final StreamCodec<FriendlyByteBuf, FeedbackEffectPacket> CODEC = StreamCodec.of(
			(buf, packet) -> {
				buf.writeIdentifier(packet.effect());

				switch (packet.origin()) {
					case null -> buf.writeByte(0);
					case Origin.Position(Vec3 pos) -> {
						buf.writeByte(1);
						buf.writeDouble(pos.x);
						buf.writeDouble(pos.y);
						buf.writeDouble(pos.z);
					}
					case Origin.EntityOrigin(int entityId) -> {
						buf.writeByte(2);
						buf.writeVarInt(entityId);
					}
				}

				buf.writeVarInt(packet.groups().size());
				packet.groups().forEach(buf::writeIdentifier);

				buf.writeVarInt(packet.params().size());
				packet.params().forEach((name, value) -> {
					buf.writeUtf(name, MAX_PARAM_NAME_LENGTH);
					buf.writeFloat(value);
				});
			},
			buf -> {
				Identifier effect = buf.readIdentifier();

				Origin origin = switch (buf.readByte()) {
					case 0 -> null;
					case 1 -> new Origin.Position(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
					case 2 -> new Origin.EntityOrigin(buf.readVarInt());
					default -> throw new IllegalArgumentException("Unknown feedback origin type");
				};

				int groupCount = buf.readVarInt();
				if (groupCount < 0 || groupCount > MAX_GROUPS) {
					throw new IllegalArgumentException("Too many feedback groups: " + groupCount);
				}
				List<Identifier> groups = new ArrayList<>(groupCount);
				for (int i = 0; i < groupCount; i++) {
					groups.add(buf.readIdentifier());
				}

				int paramCount = buf.readVarInt();
				if (paramCount < 0 || paramCount > MAX_PARAMS) {
					throw new IllegalArgumentException("Too many feedback params: " + paramCount);
				}
				Map<String, Float> params = new LinkedHashMap<>();
				for (int i = 0; i < paramCount; i++) {
					String name = buf.readUtf(MAX_PARAM_NAME_LENGTH);
					float value = buf.readFloat();
					params.put(name, Float.isFinite(value) ? value : 0f);
				}

				return new FeedbackEffectPacket(effect, origin, groups, params);
			}
	);

	public List<FeedbackGroup> feedbackGroups() {
		return this.groups.stream().map(FeedbackGroup::new).toList();
	}

	/// Where the effect comes from, resolved every frame. The supplier returns null while the origin
	/// cannot be found (no level, or the entity is not loaded or has gone), which ends the playback.
	public @Nullable Supplier<Vec3> position() {
		return switch (this.origin) {
			case null -> null;
			case Origin.Position(Vec3 pos) -> () -> pos;
			case Origin.EntityOrigin(int entityId) -> () -> {
				var level = Minecraft.getInstance().level;
				if (level == null) {
					return null;
				}
				Entity entity = level.getEntity(entityId);
				return entity == null ? null : entity.position();
			};
		};
	}
}
