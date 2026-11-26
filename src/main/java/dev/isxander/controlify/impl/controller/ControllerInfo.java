/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller;

import dev.isxander.controlify.impl.controllermanager.ControllerType;
import dev.isxander.controlify.impl.controllermanager.UniqueControllerID;
import dev.isxander.controlify.impl.hid.HIDID;

public record ControllerInfo(UniqueControllerID ucid, ControllerType type, HIDID hid) {
}
