/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.api.action.Gesture;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.action.Accumulator;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/// One leaf position of an activator tree: an instance per binding, merged into a single output.
public final class LeafSlot implements Activator {
	private final LeafConfig.Kind kind;
	private final boolean pulseOnStart;
	private final List<Instance> instances;
	private boolean mergedActive;
	private float mergedValue;

	public LeafSlot(LeafConfig config, List<ActionBinding> bindings) {
		this.kind = config.kind();
		this.pulseOnStart = !(config instanceof SpanLeafConfig span) || span.pulseOnStart();
		// A binding listed twice must not pulse twice.
		this.instances = bindings.stream()
				.map(binding -> new Key(binding.input(), config.gestureFor(binding).orElse(null)))
				.distinct()
				.map(key -> new Instance(key.input(), key.gesture()))
				.toList();
	}

	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		if (!(signal instanceof LogicalSignal.InputSignal inputSignal)) {
			return;
		}
		for (Instance instance : this.instances) {
			if (instance.input.equals(inputSignal.input())) {
				instance.accept(this.kind, inputSignal);
			}
		}

		switch (this.kind) {
			case PULSE -> {
				for (Instance instance : this.instances) {
					for (; instance.pulses > 0; instance.pulses--) {
						acc.firePulse();
					}
				}
			}
			case SPAN -> {
				boolean active = this.instances.stream().anyMatch(instance -> instance.active);
				if (active != this.mergedActive) {
					this.mergedActive = active;
					acc.setLatch(active);
					if (active && this.pulseOnStart) {
						acc.firePulse();
					}
				}
			}
			case CONTINUOUS -> {
				float value = 0f;
				for (Instance instance : this.instances) {
					value = Math.max(value, instance.value);
				}
				if (value != this.mergedValue) {
					this.mergedValue = value;
					acc.setContinuous(value);
				}
			}
		}
	}

	@Override
	public String describe() {
		return this.kind + this.instances.stream()
				.map(instance -> instance.gesture == null ? instance.input.toString() : instance.gesture.getSerializedName() + " " + instance.input)
				.collect(Collectors.joining(", ", "[", "]"));
	}

	private record Key(LogicalInput input, @Nullable Gesture gesture) {
	}

	private static final class Instance {
		private final LogicalInput input;
		private final @Nullable Gesture gesture;
		private int pulses;
		private boolean active;
		private float value;

		private Instance(LogicalInput input, @Nullable Gesture gesture) {
			this.input = input;
			this.gesture = gesture;
		}

		private void accept(LeafConfig.Kind kind, LogicalSignal.InputSignal signal) {
			switch (kind) {
				case PULSE -> {
					if (firesOn(this.gesture, signal)) {
						this.pulses++;
					}
				}
				case SPAN -> {
					if (signal instanceof LogicalSignal.ButtonRelease || signal instanceof LogicalSignal.Cancelled) {
						this.active = false;
					} else if (this.gesture == Gesture.WHILE_PRESSED && signal instanceof LogicalSignal.ButtonPress
							|| this.gesture == Gesture.WHILE_HELD && signal instanceof LogicalSignal.Held) {
						this.active = true;
					}
				}
				case CONTINUOUS -> {
					if (signal instanceof LogicalSignal.AxisMoved axis) {
						this.value = axis.value();
					} else if (signal instanceof LogicalSignal.Cancelled) {
						this.value = 0f;
					}
				}
			}
		}

		private static boolean firesOn(@Nullable Gesture gesture, LogicalSignal.InputSignal signal) {
			return gesture != null && switch (gesture) {
				case PRESS -> signal instanceof LogicalSignal.ButtonPress;
				case RELEASE -> signal instanceof LogicalSignal.ButtonRelease;
				case TAP -> signal instanceof LogicalSignal.Tapped;
				case DOUBLE_TAP -> signal instanceof LogicalSignal.DoubleTapped;
				case HOLD -> signal instanceof LogicalSignal.Held;
				case GUI_PRESS -> signal instanceof LogicalSignal.GuiPress;
				case WHILE_PRESSED, WHILE_HELD -> false;
			};
		}
	}
}
