/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.input.mapping;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

final class MappingEntryCodecs {

	private static final Codec<MappingEntryStub> CODEC_STUB = RecordCodecBuilder.create(instance -> instance.group(
			MapType.CODEC.fieldOf("input_type").forGetter(MappingEntryStub::inputType),
			MapType.CODEC.fieldOf("output_type").forGetter(MappingEntryStub::outputType)
	).apply(instance, MappingEntryStub::new));

	public static final Codec<MappingEntry.FromButton.ToButton> CODEC_BUTTON_TO_BUTTON = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromButton.ToButton::sourceInput),
			Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromButton.ToButton::to),
			Codec.BOOL.fieldOf("invert").forGetter(MappingEntry.FromButton.ToButton::invert),
			MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromButton.ToButton::inputType),
			MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromButton.ToButton::outputType)
	).apply(instance, MappingEntry.FromButton.ToButton::new));

	public static final Codec<MappingEntry.FromButton.ToAxis> CODEC_BUTTON_TO_AXIS = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromButton.ToAxis::sourceInput),
			Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromButton.ToAxis::to),
			Codec.FLOAT.fieldOf("off_state").forGetter(MappingEntry.FromButton.ToAxis::offState),
			Codec.FLOAT.fieldOf("on_state").forGetter(MappingEntry.FromButton.ToAxis::onState),
			MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromButton.ToAxis::inputType),
			MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromButton.ToAxis::outputType)
	).apply(instance, MappingEntry.FromButton.ToAxis::new));

	public static final Codec<MappingEntry.FromAxis.ToButton> CODEC_AXIS_TO_BUTTON = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromAxis.ToButton::sourceInput),
			Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromAxis.ToButton::to),
			Codec.FLOAT.fieldOf("threshold").forGetter(MappingEntry.FromAxis.ToButton::threshold),
			MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromAxis.ToButton::inputType),
			MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromAxis.ToButton::outputType)
	).apply(instance, MappingEntry.FromAxis.ToButton::new));

	public static final Codec<MappingEntry.FromAxis.ToAxis> CODEC_AXIS_TO_AXIS = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromAxis.ToAxis::sourceInput),
			Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromAxis.ToAxis::to),
			Codec.FLOAT.fieldOf("min_in").forGetter(MappingEntry.FromAxis.ToAxis::minIn),
			Codec.FLOAT.fieldOf("min_out").forGetter(MappingEntry.FromAxis.ToAxis::minOut),
			Codec.FLOAT.fieldOf("max_in").forGetter(MappingEntry.FromAxis.ToAxis::maxIn),
			Codec.FLOAT.fieldOf("max_out").forGetter(MappingEntry.FromAxis.ToAxis::maxOut),
			MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromAxis.ToAxis::inputType),
			MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromAxis.ToAxis::outputType)
	).apply(instance, MappingEntry.FromAxis.ToAxis::new));

	public static final Codec<MappingEntry> CODEC = new Codec<>() {
		@Override
		public <T> DataResult<T> encode(MappingEntry input, DynamicOps<T> ops, T prefix) {
			return switch (input) {
				case MappingEntry.FromButton.ToButton e -> CODEC_BUTTON_TO_BUTTON.encode(e, ops, prefix);
				case MappingEntry.FromButton.ToAxis e -> CODEC_BUTTON_TO_AXIS.encode(e, ops, prefix);
				case MappingEntry.FromAxis.ToButton e -> CODEC_AXIS_TO_BUTTON.encode(e, ops, prefix);
				case MappingEntry.FromAxis.ToAxis e -> CODEC_AXIS_TO_AXIS.encode(e, ops, prefix);
			};
		}

		@Override
		public <T> DataResult<Pair<MappingEntry, T>> decode(DynamicOps<T> ops, T input) {
			// First decode the stub to determine types
			return CODEC_STUB.parse(ops, input).flatMap(stub -> {
				Codec<? extends MappingEntry> codec = switch (stub.inputType()) {
					case BUTTON -> switch (stub.outputType()) {
						case BUTTON -> CODEC_BUTTON_TO_BUTTON;
						case AXIS -> CODEC_BUTTON_TO_AXIS;
					};
					case AXIS -> switch (stub.outputType()) {
						case BUTTON -> CODEC_AXIS_TO_BUTTON;
						case AXIS -> CODEC_AXIS_TO_AXIS;
					};
				};
				return codec.decode(ops, input)
						.map(pair -> pair.mapFirst(entry -> entry));
			});
		}
	};


	private record MappingEntryStub(MapType inputType, MapType outputType) {
	}

	private MappingEntryCodecs() {
	}

}
