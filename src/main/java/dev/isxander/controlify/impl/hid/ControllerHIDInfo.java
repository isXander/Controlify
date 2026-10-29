/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.hid;

import com.google.common.primitives.Ints;
import dev.isxander.controlify.impl.controllermanager.ControllerType;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

public record ControllerHIDInfo(ControllerType type, Optional<HIDDevice> hidDevice) {
	public Optional<String> createControllerUID(int controllerIndex) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		md.update(Ints.toByteArray(controllerIndex));
		hidDevice.ifPresent(hid -> {
			md.update(Ints.toByteArray(hid.vendorId()));
			md.update(Ints.toByteArray(hid.productId()));
		});

		String namespace = type().id().toString();
		if ("controlify".equals(type().id().getNamespace())) {
			// maintains backwards compatibility
			namespace = type().id().getPath();
		}

		md.update(namespace.getBytes());

		return Optional.of(UUID.nameUUIDFromBytes(md.digest()).toString());
	}
}
