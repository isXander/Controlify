/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.capsense;

import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.input.pipeline.InputPipeline;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/// Tracks the live state of capacitive touch sensors.
/// Touch state is device state only; it never becomes a logical input.
public class CapSenseComponent extends ControllerComponent {
	public static final Identifier ID = CUtil.rl("capsense");

	private final InputPipeline inputPipeline;
	private final Set<Identifier> sensors;
	private final Set<Identifier> touched = new HashSet<>();
	private @Nullable EventSource.Subscription subscription;

	public CapSenseComponent(InputPipeline inputPipeline, Set<Identifier> sensors) {
		super(ID);
		this.inputPipeline = Objects.requireNonNull(inputPipeline);
		this.sensors = Set.copyOf(sensors);
	}

	@Override
	public void attach(ControllerImpl controller) {
		super.attach(controller);

		this.subscription = this.inputPipeline.sourceProcessedPhysicalSignals().subscribe(signal -> {
			if (signal instanceof PhysicalInputSignal.CapSenseChanged(_, Identifier input, boolean isTouched)) {
				if (isTouched) {
					this.touched.add(input);
				} else {
					this.touched.remove(input);
				}
			}
		});
	}

	/// @return the capacitive touch sensors this controller supports
	public Set<Identifier> sensors() {
		return this.sensors;
	}

	/// @return whether the given sensor is currently being touched
	public boolean isTouched(Identifier sensor) {
		return this.touched.contains(sensor);
	}

	@Override
	public void close() {
		if (this.subscription != null) {
			this.subscription.close();
			this.subscription = null;
		}
		this.touched.clear();
		super.close();
	}
}
