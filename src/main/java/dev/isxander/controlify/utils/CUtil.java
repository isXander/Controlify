/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils;

import com.mojang.blaze3d.Blaze3D;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import net.minecraft.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CUtil {
	public static final ControlifyLogger LOGGER = ControlifyLogger.createMasterLogger(LoggerFactory.getLogger("Controlify"));

	public static Identifier rl(String path) {
		return Identifier.fromNamespaceAndPath("controlify", path);
	}

	public static void openUri(String uri) {
		//? if >=26.3 {
		Blaze3D.openUri(URI.create(uri));
		//?} else {
		/*Util.getPlatform().openUri(uri);
		*///?}
	}

	public static <T> Supplier<T> lazyInit(Supplier<T> supplier) {
		return new Supplier<>() {
			private T created = null;

			@Override
			public T get() {
				if (created == null)
					created = supplier.get();
				return created;
			}
		};
	}

	public static String createUIDFromBytes(byte[]... bytes) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Could not get MD5 hash.", e);
		}

		for (byte[] b : bytes)
			md.update(b);
		byte[] digest = md.digest();
		return Hex.encodeHexString(digest);
	}

	public static void sleepChecked(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOGGER.error("Failed to sleep for {}ms", e, millis);
		}
	}

	public static <T extends StringRepresentable> Function<String, T> createNameLookup(T[] values, Function<String, String> keyFunction) {
		Map<String, T> map = Arrays.stream(values)
				.collect(
						Collectors.toMap(stringRepresentable -> keyFunction.apply(stringRepresentable.getSerializedName()), stringRepresentable -> stringRepresentable)
				);
		return string -> string == null ? null : map.get(string);
	}


	public static float positiveAxis(float value) {
		return value < 0 ? 0 : value;
	}

	public static float negativeAxis(float value) {
		return value > 0 ? 0 : -value;
	}

	public static float mapShortToFloat(short value) {
		// we need to do this since signed short range / 2 != 0
		return Mth.clampedMap(value, Short.MIN_VALUE, 0, -1f, 0f)
			+ Mth.clampedMap(value, 0, Short.MAX_VALUE, 0f, 1f);
	}

}
