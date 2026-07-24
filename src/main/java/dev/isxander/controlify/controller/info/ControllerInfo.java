/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.info;

import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controllermanager.UniqueControllerID;
import dev.isxander.controlify.hid.HIDDevice;

import java.util.Optional;

public record ControllerInfo(UniqueControllerID ucid, ControllerType type, Optional<HIDDevice> hid) {
}
