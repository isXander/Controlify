/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.hid;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.utils.codec.CExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.HexFormat;

public record HIDID(int vendorId, int productId) {
	public static final Codec<HIDID> CODEC = CExtraCodecs.arrayPair(
			Codec.INT,
			HIDID::vendorId, HIDID::productId,
			HIDID::new
	);

	@Override
	public @NotNull String toString() {
		var hex = HexFormat.of();
		return "HID[" +
				"VID=0x" + hex.toHexDigits(vendorId, 4) +
				", PID=0x" + hex.toHexDigits(productId, 4) +
				']';
	}
}
