package dev.isxander.controlify.api.controller.dualsense;

public interface DualsenseComponent {
	/// Set the state of the controller's mute LED to either lit or unlit
	void setMicLightMode(MicLightMode mode);
}
