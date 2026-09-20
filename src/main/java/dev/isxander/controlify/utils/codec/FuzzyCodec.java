/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class FuzzyCodec<T> implements Codec<T> {
	private final List<Codec<? extends T>> codecs;
	private final Function<T, Encoder<? extends T>> encoderGetter;

	public FuzzyCodec(List<Codec<? extends T>> codecs, Function<T, Encoder<? extends T>> encoderGetter) {
		this.codecs = codecs;
		this.encoderGetter = encoderGetter;
	}

	@Override
	public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
		for (Decoder<? extends T> decoder : this.codecs) {
			DataResult<? extends Pair<? extends T, T1>> result = decoder.decode(ops, input);
			if (result.result().isPresent()) {
				return (DataResult<Pair<T, T1>>) result;
			}
		}

		return DataResult.error(() -> "No matching codec found.");
	}

	@Override
	public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
		Encoder<T> encoder = (Encoder<T>) this.encoderGetter.apply(input);
		return encoder.encode(input, ops, prefix);
	}
}
