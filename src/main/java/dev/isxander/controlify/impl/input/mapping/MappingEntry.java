/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.mapping;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import dev.isxander.controlify.impl.utils.MthExt;
import net.minecraft.resources.Identifier;

public sealed interface MappingEntry {
	/// Transforms an input signal
	/// A mapping entry may transform a signal to one with a differing input identifier.
	/// This function will only be called when the signal matches {@link sourceInput}.
	PhysicalInputSignal apply(PhysicalInputSignal signal);

	/// The physical input identifier this mapping entry transforms.
	/// The {@link apply} function will only receive signals whose {@link PhysicalInputSignal#input()} matches.
	Identifier sourceInput();

	MapType inputType();

	MapType outputType();

	static Codec<MappingEntry> codec() {
		return MappingEntryCodecs.CODEC;
	}

	sealed interface FromButton extends MappingEntry {
		record ToButton(Identifier sourceInput, Identifier to, boolean invert, MapType inputType, MapType outputType) implements FromButton {
			public ToButton(Identifier sourceInput, Identifier to, boolean invert) {
				this(sourceInput, to, invert, MapType.BUTTON, MapType.BUTTON);
			}

			@Override
			public PhysicalInputSignal apply(PhysicalInputSignal signal) {
				if (signal instanceof PhysicalInputSignal.ButtonChanged(long timeNanos, _, boolean state)) {
					return new PhysicalInputSignal.ButtonChanged(timeNanos, to, invert != state);
				}
				return signal;
			}
		}

		record ToAxis(Identifier sourceInput, Identifier to, float offState, float onState, MapType inputType, MapType outputType) implements FromButton {
			public ToAxis(Identifier sourceInput, Identifier to, float offState, float onState) {
				this(sourceInput, to, offState, onState, MapType.BUTTON, MapType.AXIS);
			}

			@Override
			public PhysicalInputSignal apply(PhysicalInputSignal signal) {
				if (signal instanceof PhysicalInputSignal.ButtonChanged(long timeNanos, _, boolean state)) {
					return new PhysicalInputSignal.AxisMoved(timeNanos, this.to, state ? onState : offState);
				}
				return signal;
			}
		}
	}

	sealed interface FromAxis extends MappingEntry {
		record ToButton(Identifier sourceInput, Identifier to, float threshold, MapType inputType, MapType outputType) implements FromAxis {
			public ToButton(Identifier sourceInput, Identifier to, float threshold) {
				this(sourceInput, to, threshold, MapType.AXIS, MapType.BUTTON);
			}

			@Override
			public PhysicalInputSignal apply(PhysicalInputSignal signal) {
				if (signal instanceof PhysicalInputSignal.AxisMoved(long timeNanos, _, float value)) {
					return new PhysicalInputSignal.ButtonChanged(timeNanos, to, value >= threshold);
				}
				return signal;
			}
		}

		record ToAxis(Identifier sourceInput, Identifier to, float minIn, float minOut, float maxIn, float maxOut, MapType inputType, MapType outputType) implements FromAxis {
			public ToAxis(Identifier sourceInput, Identifier to, float minIn, float minOut, float maxIn, float maxOut) {
				this(sourceInput, to, minIn, minOut, maxIn, maxOut, MapType.AXIS, MapType.AXIS);
			}

			@Override
			public PhysicalInputSignal apply(PhysicalInputSignal signal) {
				if (signal instanceof PhysicalInputSignal.AxisMoved(long timeNanos, _, float value)) {
					float clampedInput = Math.clamp(value, Math.min(minIn, maxIn), Math.max(minIn, maxIn));
					float output = minIn == maxIn ? minOut : MthExt.remap(clampedInput, minIn, maxIn, minOut, maxOut);
					return new PhysicalInputSignal.AxisMoved(timeNanos, to, Math.clamp(output, 0f, 1f));
				}
				return signal;
			}
		}
	}
}
