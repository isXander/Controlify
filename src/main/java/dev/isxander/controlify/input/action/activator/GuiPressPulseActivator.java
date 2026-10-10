package dev.isxander.controlify.input.action.activator;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.Activator;
import dev.isxander.controlify.input.logical.LogicalInput;
import dev.isxander.controlify.input.logical.LogicalSignal;

/// Produces a pulse on release when no GUI navigation consumed the press.
public record GuiPressPulseActivator(LogicalInput boundInput) implements Activator {
	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		if (signal instanceof LogicalSignal.GuiPress(_, LogicalInput input) && this.boundInput.equals(input)) {
			acc.firePulse();
		}
	}

	@Override
	public String describe() {
		return "GuiPress[" + boundInput.toString() + "]";
	}

	public record Config() implements ActivatorConfig<GuiPressPulseActivator> {
		public static final String TYPE_ID = "pulse_gui_press";
		public static final Config INSTANCE = new Config();
		public static final MapCodec<Config> MAP_CODEC = MapCodec.unit(INSTANCE);

		@Override
		public GuiPressPulseActivator create(LogicalInput boundInput) {
			return new GuiPressPulseActivator(boundInput);
		}

		@Override
		public ActivatorConfigType<GuiPressPulseActivator, Config> type() {
			return ActivatorConfigType.GUI_PRESS_PULSE;
		}
	}
}
