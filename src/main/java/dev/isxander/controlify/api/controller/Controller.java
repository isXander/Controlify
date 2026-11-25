/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.controller;

import dev.isxander.controlify.api.contextual.GuideVerbosity;
import dev.isxander.controlify.api.controller.dualsense.DualsenseComponent;
import dev.isxander.controlify.api.controller.input.InputComponent;
import dev.isxander.controlify.api.controller.gyro.GyroComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/// The API-safe controller instance.
@ApiStatus.NonExtendable
public interface Controller {
	/// The unique identifier of this controller.
	/// The same physical controller should have the same
	/// identifier after being unplugged and replugged.
	String uid();

	/// The friendly name of this controller.
	///
	/// This may use {@link driverReportedName}, but the
	/// controller's type may override it, or the user may override
	/// it.
	///
	/// Because the user may override this, it is not a
	/// guarantee that this name is constant for the lifetime
	/// of this controller object.
	String name();

	/// The friendly name of this controller reported by
	/// the underlying controller driver.
	///
	/// To get the display name of this controller, you
	/// should use {@link name}.
	String driverReportedName();

	/// The identifier of this controller's type.
	/// This is the same ID defined by `controller_identification.json5`.
	Identifier typeId();

	/// How much the player has chosen for button guides to show with this controller.
	/// Return it from [dev.isxander.controlify.api.contextual.Context#verbosity()] in a custom context.
	GuideVerbosity guideVerbosity();

	/// Input capabilities and current actions.
	InputComponent input();

	/// Present if this controller supports gyroscope input.
	Optional<? extends GyroComponent> gyro();

	/// Present if this controller is a dualsense controller.
	/// You can configure the mute light.
	Optional<? extends DualsenseComponent> dualsense();
}
