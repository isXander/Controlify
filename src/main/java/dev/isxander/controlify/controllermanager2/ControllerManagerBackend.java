package dev.isxander.controlify.controllermanager2;

import dev.isxander.controlify.controller.ControllerEntity;

public interface ControllerManagerBackend {

	void setup();

	void pollEvents();

	void closeController(ControllerEntity controller);
}
