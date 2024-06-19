package dev.isxander.controlify.controller;

import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controllermanager.UniqueControllerID;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDIdentifier;

import java.util.Optional;

public record ControllerInfo(String uid, UniqueControllerID ucid, String guid, String driverName, ControllerType type, Optional<HIDDevice> hid) {
}
