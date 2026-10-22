package dev.isxander.controlify.impl.action;

/// Applies emulated output without changing Minecraft's keyboard toggle policy.
public interface KeyMappingHandle {
	void controlify$click();

	void controlify$setDown(boolean down);
}
