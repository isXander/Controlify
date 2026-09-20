/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework.controller;

import dev.isxander.controlify.driver.dualsense.DualsenseEffectsState;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

/**
 * A virtual DualSense firmware implementation for game tests.
 *
 * <p>Effect packets are commands: their enable bits specify which fields the
 * controller should update. This driver retains the enabled values so tests
 * can inspect the effective controller state instead of a transient packet.</p>
 */
public final class DualsenseEffectDriver implements GamepadEffectDriver<DualsenseEffectsState> {
	private final DualsenseEffectsState firmwareState = new DualsenseEffectsState();

	@Override
	public DualsenseEffectsState receiveEffect(ByteBuffer effect) {
		DualsenseEffectsState command = DualsenseEffectsState.readFrom(
			MemorySegment.ofBuffer(effect.duplicate())
		);

		if (isEnabled(command.ucEnableBits1, DualsenseEffectsState.EnableBitFlags1.ALLOW_LEFT_TRIGGER_FFB)) {
			this.firmwareState.rgucLeftTriggerEffect = command.rgucLeftTriggerEffect;
		}
		if (isEnabled(command.ucEnableBits1, DualsenseEffectsState.EnableBitFlags1.ALLOW_RIGHT_TRIGGER_FFB)) {
			this.firmwareState.rgucRightTriggerEffect = command.rgucRightTriggerEffect;
		}
		if (isEnabled(command.ucEnableBits2, DualsenseEffectsState.EnableBitFlags2.ALLOW_MUTE_LIGHT)) {
			this.firmwareState.ucMicLightMode = command.ucMicLightMode;
		}

		return this.firmwareState;
	}

	@Override
	public ByteBuffer sendEffect(DualsenseEffectsState effect) {
		byte[] bytes = new byte[(int) DualsenseEffectsState.LAYOUT.byteSize()];
		MemorySegment memory = MemorySegment.ofArray(bytes);
		effect.writeTo(memory);
		return memory.asByteBuffer();
	}

	private static boolean isEnabled(byte flags, byte flag) {
		return (flags & flag) != 0;
	}
}
