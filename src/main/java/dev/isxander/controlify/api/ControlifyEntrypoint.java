package dev.isxander.controlify.api;

/// Registers addon event listeners before Controlify starts client setup.
/// Use Fabric's `controlify` entrypoint or Java's ServiceLoader on either loader.
/// Called once per provider class, client-side, after platform services are installed.
/// Minecraft and Controlify's managers are not yet available; obtain managers from events.
@FunctionalInterface
public interface ControlifyEntrypoint {
	void onControlifyInitialize();
}
