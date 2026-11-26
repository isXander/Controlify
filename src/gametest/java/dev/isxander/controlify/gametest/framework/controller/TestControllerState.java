/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

public class TestControllerState<E> implements TestControllerStateView<E> {
	private int playerIndex;
	private short lowFrequencyRumble, highFrequencyRumble;
	private short leftTriggerRumble, rightTriggerRumble;
	private byte ledRed, ledGreen, ledBlue;
	private @Nullable ByteBuffer latestEffect;
	private @Nullable E effectState;
	private boolean sensorsEnabled;

	private final GamepadEffectDriver<E> effectDriver;

	public TestControllerState(GamepadEffectDriver<E> effectDriver) {
		this.effectDriver = effectDriver;
	}

	@Override
	public synchronized int getPlayerIndex() {
		return this.playerIndex;
	}

	public synchronized void setPlayerIndex(int playerIndex) {
		this.playerIndex = playerIndex;
	}

	@Override
	public synchronized short getLowFrequencyRumble() {
		return this.lowFrequencyRumble;
	}

	@Override
	public synchronized short getHighFrequencyRumble() {
		return highFrequencyRumble;
	}

	public synchronized void setRumble(short lowFrequencyRumble, short highFrequencyRumble) {
		this.lowFrequencyRumble = lowFrequencyRumble;
		this.highFrequencyRumble = highFrequencyRumble;
	}

	@Override
	public synchronized short getLeftTriggerRumble() {
		return leftTriggerRumble;
	}

	@Override
	public synchronized short getRightTriggerRumble() {
		return rightTriggerRumble;
	}

	public synchronized void setTriggerRumble(short leftTriggerRumble, short rightTriggerRumble) {
		this.leftTriggerRumble = leftTriggerRumble;
		this.rightTriggerRumble = rightTriggerRumble;
	}

	@Override
	public synchronized byte getLedRed() {
		return ledRed;
	}

	@Override
	public synchronized byte getLedGreen() {
		return ledGreen;
	}

	@Override
	public synchronized byte getLedBlue() {
		return ledBlue;
	}

	public synchronized void setLed(byte red, byte green, byte blue) {
		this.ledRed = red;
		this.ledGreen = green;
		this.ledBlue = blue;
	}

	@Override
	public synchronized @Nullable ByteBuffer getLatestEffect() {
		return latestEffect;
	}

	public synchronized void setLatestEffect(@NotNull ByteBuffer latestEffect) {
		this.latestEffect = latestEffect;
		this.effectState = this.effectDriver.receiveEffect(latestEffect);
	}

	@Override
	public synchronized @Nullable E getGamepadEffectState() {
		return effectState;
	}

	@Override
	public synchronized boolean getSensorsEnabled() {
		return sensorsEnabled;
	}

	public synchronized void setSensorsEnabled(boolean sensorsEnabled) {
		this.sensorsEnabled = sensorsEnabled;
	}
}
