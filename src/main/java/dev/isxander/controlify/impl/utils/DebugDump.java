/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.utils;

import com.sun.jna.Platform;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.input.InputComponentImpl;
import dev.isxander.controlify.impl.controllermanager.ControllerBackend;
import dev.isxander.controlify.impl.controllermanager.ControllerCoordinator;
import dev.isxander.controlify.impl.controllermanager.ControllerManager;
import dev.isxander.controlify.impl.platform.Environment;
import dev.isxander.controlify.impl.platform.main.PlatformMainUtil;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DebugDump {
	public static String dumpDebug() {
		IndentedStringBuilder dump = new IndentedStringBuilder();

		LocalDateTime dateTime = LocalDateTime.now();
		String formattedDate = dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
		dump.line("CONTROLIFY DEBUG DUMP - ", formattedDate, '\n');

		dump.line("Controlify version: ", PlatformMainUtil.getControlifyVersion());
		dump.line("Minecraft version: ", SharedConstants.getCurrentVersion().name());

		if (PlatformMainUtil.getEnv() == Environment.CLIENT) {
			dump.line("Client").pushIndent();
			dumpClientDebug(dump);
			dump.popIndent();
		}

		return dump.build();
	}

	private static void dumpClientDebug(IndentedStringBuilder dump) {
		dump.line("Platform: ", Platform.RESOURCE_PREFIX);
		dump.line();

		Optional<ControllerCoordinator> controllerCoordinatorOpt = Controlify.instance().getControllerCoordinator();
		if (controllerCoordinatorOpt.isPresent()) {
			ControllerCoordinator controllerCoordinator = controllerCoordinatorOpt.get();
			ControllerBackend backend = controllerCoordinator.backend();
			dump.line("Controller backend: ", backend != null ? backend.getClass().getName() : "null");

			Optional<ControllerManager> controllerManagerOpt = Controlify.instance().getControllerManager();
			if (controllerManagerOpt.isPresent()) {
				ControllerManager controllerManager = controllerManagerOpt.get();

				dump.line("Connected Controllers:").pushIndent();
				for (ControllerImpl controller : controllerManager.getAllControllers()) {
					dump.line("Name: ", controller.name());
					dump.line("Identified type: ", controller.info().type());
					dump.line("UID: ", controller.uid());
					dump.line("UCID: ", controller.info().ucid());
					dump.line("HID: ", controller.info().hid());

					InputComponentImpl input = controller.input();
					dump.line("Input Component:").pushIndent();
					dump.line("Definitely gamepad: ", input.isGamepad());
					dump.line("Available inputs:").pushIndent();
					for (Identifier button : input.physicalButtons()) {
						dump.line("BTN ", button);
					}
					for (Identifier axis : input.physicalAxes()) {
						dump.line("AXS ", axis);
					}
					dump.popIndent(); // available inputs

					dump.popIndent(); // input component

					dump.line("Rumble supported: ", controller.rumble().isPresent());
					dump.line("Trigger rumble supported: ", controller.triggerRumble().isPresent());
					dump.line("Battery level: ", controller.batteryLevel().map(b -> b.getBatteryLevel().toString()).orElse("UNSUPPORTED"));
					dump.line("Gyro supported: ", controller.gyro().isPresent());
					dump.line("Touchpads supported: ", controller.touchpad().map(touchpad -> touchpad.touchpads().length).orElse(0));
					dump.line("HD haptics supported: ", controller.hdHaptics().isPresent());
					dump.line("Log:").pushIndent().line(controller.logger().export()).popIndent();

					dump.line();
				}
				dump.popIndent(); // connected controllers
			}
		}
	}

	private static class IndentedStringBuilder {
		private final StringBuilder sb = new StringBuilder();
		private int indent;

		public IndentedStringBuilder line(Object... parts) {
			sb.append("  ".repeat(indent));
			for (Object part : parts) {
				String stringified = part.toString()
						.replace("\n", "\n" + "  ".repeat(indent));

				sb.append(stringified);
			}
			sb.append('\n');

			return this;
		}

		public IndentedStringBuilder pushIndent() {
			indent++;
			return this;
		}

		public IndentedStringBuilder popIndent() {
			indent--;
			return this;
		}

		public String build() {
			return sb.toString();
		}
	}
}
