package dev.isxander.controlify.gametest.framework;

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
