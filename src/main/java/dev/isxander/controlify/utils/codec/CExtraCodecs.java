/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public final class CExtraCodecs {

	public static <T> MapCodec<T> fuzzyMap(
			List<MapCodec<? extends T>> codecs,
			Function<T, MapEncoder<? extends T>> encoderGetter) {
		return new FuzzyMapCodec<>(codecs, encoderGetter);
	}

	public static <T> Codec<Set<T>> set(Codec<T> elementCodec, int minSize, int maxSize) {
		return new SetCodec<>(elementCodec, minSize, maxSize);
	}

	public static <T> Codec<Set<T>> set(Codec<T> elementCodec) {
		return new SetCodec<>(elementCodec, 0, Integer.MAX_VALUE);
	}

	public static <T> MapCodec<T> strictEitherMap(
			String typedKeyName, MapCodec<T> typed, MapCodec<T> fuzzy, boolean typedEncode
	) {
		return new StrictEitherMapCodec<>(typedKeyName, typed, fuzzy, typedEncode);
	}

	public static <T> MapCodec<@Nullable T> nullableField(Codec<T> codec, String fieldName) {
		return codec.optionalFieldOf(fieldName).xmap(
				opt -> opt.orElse(null),
				Optional::ofNullable
		);
	}

	public static <T> Codec<Pair<T, T>> arrayPair(Codec<T> elementCodec) {
		return elementCodec.listOf(2, 2).xmap(
				list -> Pair.of(list.get(0), list.get(1)),
				pair -> List.of(pair.getFirst(), pair.getSecond())
		);
	}

	public static <T, R> Codec<R> arrayPair(Codec<T> elementCodec, Function<R, T> firstGetter, Function<R, T> secondGetter, BiFunction<T, T, R> constructor) {
		return arrayPair(elementCodec).xmap(
				pair -> constructor.apply(pair.getFirst(), pair.getSecond()),
				obj -> Pair.of(firstGetter.apply(obj), secondGetter.apply(obj))
		);
	}

	public static <E> MapCodec<E> orCompressed(MapCodec<E> first, MapCodec<E> second) {
		return new MapCodec<>() {
			@Override
			public <T> RecordBuilder<T> encode(E object, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
				return dynamicOps.compressMaps() ? second.encode(object, dynamicOps, recordBuilder) : first.encode(object, dynamicOps, recordBuilder);
			}

			@Override
			public <T> DataResult<E> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
				return dynamicOps.compressMaps() ? second.decode(dynamicOps, mapLike) : first.decode(dynamicOps, mapLike);
			}

			@Override
			public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
				return second.keys(dynamicOps);
			}

			public String toString() {
				return first + " orCompressed " + second;
			}
		};
	}

	public static <E> Codec<E> stringResolver(final Function<E, String> toString, final Function<String, E> fromString) {
		return Codec.STRING.flatXmap(
			name -> Optional.ofNullable(fromString.apply(name)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown element name:" + name)),
			e -> Optional.ofNullable(toString.apply(e)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Element with unknown name: " + e))
		);
	}

	private CExtraCodecs() {}
}
