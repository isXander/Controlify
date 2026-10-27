package dev.isxander.controlify.api.controller.dualsense;

public enum MicLightMode {
	OFF,
	ON,
	BREATHING;

	public static MicLightMode fromBoolean(boolean on) {
		return on ? ON : OFF;
	}
}
