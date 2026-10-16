package dev.isxander.controlify.controllermanager;

import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.driver.steamdeck.SteamDeckUtil;
import dev.isxander.controlify.hid.HIDID;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.log.ControlifyLogger;

import java.io.Closeable;
import java.util.*;

public class ControllerManager implements Closeable {
	private final Map<String, ControllerEntity> controllersByUid;
	private final Map<UniqueControllerID, ControllerEntity> controllersByUcid;

	public ControllerManager() {
		this.controllersByUid = new HashMap<>();
		this.controllersByUcid = new HashMap<>();
	}

	public boolean canAddController(
			UniqueControllerID ucid,
			HIDID hidid,
			ControllerType controllerType,
			ControlifyLogger controllerLogger
	) {
		if (this.controllersByUcid.containsKey(ucid)) {
			controllerLogger.warn("Tried to create controller that already is initialised: {}.", ucid);
			return false;
		}

		if (controllerType.preventLoading()) {
			controllerLogger.debugLog("Preventing load of controller #" + ucid + " because its type prevents loading.");
			return false;
		}

		if (controllerType.isSteamDeck()
			&& SteamDeckUtil.DECK_MODE.isDesktopMode()
		) {
			controllerLogger.log("Preventing load of controller #{} because Steam Deck is in desktop mode.", ucid);
			return false;
		}

		return true;
	}

	public void addController(ControllerEntity controller, boolean hotplugged) {
		this.controllersByUcid.put(controller.info().ucid(), controller);
		this.controllersByUid.put(controller.uid(), controller);

		CUtil.LOGGER.log("Controller connected: {}", ControllerUtils.createControllerString(controller));

		ControllerUtils.wrapControllerError(
				() -> ControlifyEvents.CONTROLLER_CONNECTED.invoke(new ControlifyEvents.ControllerConnected(controller, hotplugged)),
				"Connecting controller",
				controller
		);
	}

	public void removeController(UniqueControllerID controllerUcid) {
		ControllerEntity controller = this.controllersByUcid.get(controllerUcid);

		ControlifyEvents.CONTROLLER_DISCONNECTED.invoke(new ControlifyEvents.ControllerDisconnected(controller));

		if (controller == null) {
			throw new IllegalArgumentException("The given ucid is not associated with any connected controller");
		}

		this.controllersByUcid.remove(controllerUcid, controller);
		this.controllersByUid.remove(controller.uid(), controller);

		controller.close();
	}

	public Optional<ControllerEntity> getControllerByUcid(UniqueControllerID ucid) {
		return Optional.ofNullable(this.controllersByUcid.get(ucid));
	}

	public Optional<ControllerEntity> getControllerByUid(String uid) {
		return Optional.ofNullable(this.controllersByUid.get(uid));
	}

	public Collection<ControllerEntity> getAllControllers() {
		return Collections.unmodifiableCollection(this.controllersByUcid.values());
	}

	@Override
	public void close() {
		this.controllersByUcid.values().forEach(ControllerEntity::close);
	}
}
