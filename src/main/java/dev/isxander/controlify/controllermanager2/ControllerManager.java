package dev.isxander.controlify.controllermanager2;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controllermanager.UniqueControllerID;
import dev.isxander.controlify.hid.HIDID;
import dev.isxander.controlify.utils.log.ControlifyLogger;

public interface ControllerManager {
	boolean canAddController(
			UniqueControllerID ucid,
			HIDID hidid,
			ControllerType controllerType,
			ControlifyLogger controllerLogger
	);

	void addController(ControllerEntity controller);

	void removeController(UniqueControllerID controller);

	ControllerType getControllerType(HIDID hidId);
}
