/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework.controller;

import java.nio.ByteBuffer;

public interface GamepadEffectDriver<E> {
	E receiveEffect(ByteBuffer effect);

	ByteBuffer sendEffect(E effect);

	class Noop implements GamepadEffectDriver<ByteBuffer> {
		@Override
		public ByteBuffer receiveEffect(ByteBuffer effect) {
			return effect;
		}

		@Override
		public ByteBuffer sendEffect(ByteBuffer effect) {
			return effect;
		}
	}
}
