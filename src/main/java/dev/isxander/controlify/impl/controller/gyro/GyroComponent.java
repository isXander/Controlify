/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.gyro;

import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.config.settings.device.DeviceSettings;
import dev.isxander.controlify.impl.config.settings.profile.GyroSettings;
import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.input.pipeline.InputPipeline;
import dev.isxander.controlify.impl.input.SensorType;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSensorSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.event.EventSource;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Objects;

public class GyroComponent extends ControllerComponent {
	public static final Identifier ID = CUtil.rl("gyro");
	private final InputPipeline inputPipeline;
	private @Nullable EventSource.Subscription calibrationSubscription;
	private @Nullable EventSource.Subscription sensorSubscription;
	private DeviceSettings deviceSettings;

	private Vector3fc gyroState = new Vector3f();
	private static final long MAX_SAMPLE_AGE_NANOS = 50_000_000L;
	private long lastStateTimeNanos;
	private final GyroCalibrator calibrator = new GyroCalibrator();

	private static final long CALIBRATION_SAVE_INTERVAL_NANOS = 5_000_000_000L;
	private long lastCalibrationSaveTimeNanos;
	private Vector3fc lastSavedBias = new Vector3f();

	public GyroComponent(InputPipeline inputPipeline) {
		super(ID);
		this.inputPipeline = Objects.requireNonNull(inputPipeline);
	}

	@Override
	public void attach(ControllerImpl controller) {
		super.attach(controller);

		// Load the calibration from device settings
		this.deviceSettings = Controlify.instance().config().getSettings()
				.getOrCreateDeviceSettings(controller.uid());
		Vector3fc savedOffset = this.deviceSettings.gyroCalibration.offset;

		// Initialize the calibrator with the saved offset
		calibrator.setBias(savedOffset);
		lastSavedBias = new Vector3f(savedOffset);
		this.lastCalibrationSaveTimeNanos = System.nanoTime();

		this.calibrationSubscription = this.inputPipeline.sourceRawPhysicalSignals().subscribe(this::onRawSignal);
		this.sensorSubscription = this.inputPipeline.sourceLogicalSignals().subscribe(this::onSignal);
	}

	private void onRawSignal(PhysicalSignal signal) {
		if (signal instanceof PhysicalSensorSignal(_, _, SensorType sensorType, float x, float y, float z)
				&& sensorType == SensorType.GYROSCOPE) {
			this.updateCalibration(x, y, z);
		}
	}

	private void updateCalibration(float x, float y, float z) {
		this.calibrator.update(x, y, z);
		// The raw tap runs before SensorProcessorStage, so its bias supplier sees this update immediately.
		this.deviceSettings.gyroCalibration.offset = this.calibrator.getCurrentBias();

		long now = System.nanoTime();
		if (now - this.lastCalibrationSaveTimeNanos >= CALIBRATION_SAVE_INTERVAL_NANOS) {
			this.lastCalibrationSaveTimeNanos = now;
			this.saveCalibrationIfChanged();
		}
	}

	private void onSignal(LogicalSignal signal) {
		if (signal instanceof LogicalSignal.SensorInterval(_, _, SensorType sensorType, float x, float y, float z)
				&& sensorType == SensorType.GYROSCOPE) {
			// The pipeline has already calibrated, filtered and integrated these values into radians.
			this.gyroState = new Vector3f(x, y, z);
			this.lastStateTimeNanos = System.nanoTime();
		}
	}

	/// Returns the latest integrated gyro interval in radians.
	/// Missing samples expire naturally, independently of controller selection.
	public Vector3fc getState() {
		return System.nanoTime() - this.lastStateTimeNanos > MAX_SAMPLE_AGE_NANOS
				? new Vector3f() : this.gyroState;
	}

	private void saveCalibrationIfChanged() {
		Vector3fc currentBias = this.deviceSettings.gyroCalibration.offset;

		// Check if bias has changed significantly (more than 0.001 rad/s on any axis)
		float threshold = 0.001f;
		if (Math.abs(currentBias.x() - lastSavedBias.x()) > threshold ||
			Math.abs(currentBias.y() - lastSavedBias.y()) > threshold ||
			Math.abs(currentBias.z() - lastSavedBias.z()) > threshold) {

			// Settings update on every sample; persistence is checked at a fixed time interval.
			Controlify.instance().config().markDirty();

			lastSavedBias = new Vector3f(currentBias);
		}
	}

	public GyroCalibrator getCalibrator() {
		return this.calibrator;
	}

	public GyroSettings settings() {
		return this.controller().settings().gyro;
	}

	public GyroSettings defaultSettings() {
		return this.controller().defaultSettings().gyro;
	}

	@Override
	public void close() {
		if (this.calibrationSubscription != null) {
			this.calibrationSubscription.close();
			this.calibrationSubscription = null;
		}
		if (this.sensorSubscription != null) {
			this.sensorSubscription.close();
			this.sensorSubscription = null;
		}
		if (this.deviceSettings != null) {
			this.saveCalibrationIfChanged();
		}
		super.close();
	}

}
