/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.test.framework;

import dev.isxander.sdl.*;

import java.util.ArrayList;
import java.util.List;

public class SdlVirtualJoystickDescBuilder {
	private int type;
	private int vendorId;
	private int productId;
	private int numAxes;
	private int numButtons;
	private int numBalls;
	private int numHats;
	private int buttonMask;
	private int axisMask;
	private String name;
	private final List<SdlVirtualJoystickTouchpadDesc> touchpads = new ArrayList<>();
	private final List<SdlVirtualJoystickSensorDesc> sensors = new ArrayList<>();
	private SdlPointer userdata;
	private SdlCallbacks.VirtualJoystickUpdateCallback update = (_) -> {};
	private SdlCallbacks.VirtualJoystickSetPlayerIndexCallback setPlayerIndex = (_, _) -> {};
	private SdlCallbacks.VirtualJoystickRumbleCallback rumble = (_, _, _) -> false;
	private SdlCallbacks.VirtualJoystickRumbleTriggersCallback rumbleTriggers = (_, _, _) -> false;
	private SdlCallbacks.VirtualJoystickSetLedCallback setLed = (_, _, _, _) -> false;
	private SdlCallbacks.VirtualJoystickSendEffectCallback sendEffect = (_, _) -> false;
	private SdlCallbacks.VirtualJoystickSetSensorsEnabledCallback setSensorsEnabled = (_, _) -> false;
	private SdlCallbacks.VirtualJoystickCleanupCallback cleanup = (_) -> {};

	public SdlVirtualJoystickDescBuilder withType(int type) {
		this.type = type;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withGamepadType() {
		return this.withType(SdlJoystick.SDL_JOYSTICK_TYPE_GAMEPAD);
	}

	public SdlVirtualJoystickDescBuilder withVendorId(int vendorId) {
		this.vendorId = vendorId;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withProductId(int productId) {
		this.productId = productId;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withNumAxes(int numAxes) {
		this.numAxes = numAxes;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withNumButtons(int numButtons) {
		this.numButtons = numButtons;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withNumBalls(int numBalls) {
		this.numBalls = numBalls;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withNumHats(int numHats) {
		this.numHats = numHats;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withButtonMask(int buttonMask) {
		this.buttonMask = buttonMask;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withAxisMask(int axisMask) {
		this.axisMask = axisMask;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withTouchpad(SdlVirtualJoystickTouchpadDesc touchpad) {
		this.touchpads.add(touchpad);
		return this;
	}

	public SdlVirtualJoystickDescBuilder withTouchpad(int numFingers) {
		return this.withTouchpad(new SdlVirtualJoystickTouchpadDesc(numFingers));
	}

	public SdlVirtualJoystickDescBuilder withSensor(SdlVirtualJoystickSensorDesc sensor) {
		this.sensors.add(sensor);
		return this;
	}

	public SdlVirtualJoystickDescBuilder withSensor(int type, float rate) {
		return this.withSensor(new SdlVirtualJoystickSensorDesc(type, rate));
	}

	public SdlVirtualJoystickDescBuilder withUserdata(SdlPointer userdata) {
		this.userdata = userdata;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withUpdate(SdlCallbacks.VirtualJoystickUpdateCallback update) {
		this.update = update;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withSetPlayerIndex(SdlCallbacks.VirtualJoystickSetPlayerIndexCallback setPlayerIndex) {
		this.setPlayerIndex = setPlayerIndex;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withRumble(SdlCallbacks.VirtualJoystickRumbleCallback rumble) {
		this.rumble = rumble;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withRumbleTriggers(SdlCallbacks.VirtualJoystickRumbleTriggersCallback rumbleTriggers) {
		this.rumbleTriggers = rumbleTriggers;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withSetLed(SdlCallbacks.VirtualJoystickSetLedCallback setLed) {
		this.setLed = setLed;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withSendEffect(SdlCallbacks.VirtualJoystickSendEffectCallback sendEffect) {
		this.sendEffect = sendEffect;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withSetSensorsEnabled(SdlCallbacks.VirtualJoystickSetSensorsEnabledCallback setSensorsEnabled) {
		this.setSensorsEnabled = setSensorsEnabled;
		return this;
	}

	public SdlVirtualJoystickDescBuilder withCleanup(SdlCallbacks.VirtualJoystickCleanupCallback cleanup) {
		this.cleanup = cleanup;
		return this;
	}

	public SdlVirtualJoystickDesc build() {
		return new SdlVirtualJoystickDesc(
			this.type,
			this.vendorId,
			this.productId,
			this.numAxes,
			this.numButtons,
			this.numBalls,
			this.numHats,
			this.buttonMask,
			this.axisMask,
			this.name,
			this.touchpads.toArray(SdlVirtualJoystickTouchpadDesc[]::new),
			this.sensors.toArray(SdlVirtualJoystickSensorDesc[]::new),
			this.userdata,
			this.update,
			this.setPlayerIndex,
			this.rumble,
			this.rumbleTriggers,
			this.setLed,
			this.sendEffect,
			this.setSensorsEnabled,
			this.cleanup
		);
	}
}
