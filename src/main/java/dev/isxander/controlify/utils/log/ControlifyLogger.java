/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils.log;

import net.minecraft.CrashReport;
import org.jetbrains.annotations.Contract;

public interface ControlifyLogger {
	void log(String message);

	void log(String message, Object... args);

	void log(String message, Throwable throwable);

	void log(String message, Throwable throwable, Object... args);

	void error(String message);

	void error(String message, Object... args);

	void error(String message, Throwable throwable);

	void error(String message, Throwable throwable, Object... args);

	void warn(String message);

	void warn(String message, Object... args);

	void warn(String message, Throwable throwable);

	void warn(String message, Throwable throwable, Object... args);

	void debugLog(String message);

	void debugLog(String message, Object... args);

	void debugLog(String message, Throwable throwable);

	void debugLog(String message, Throwable throwable, Object... args);

	void debugError(String message);

	void debugError(String message, Object... args);

	void debugError(String message, Throwable throwable);

	void debugError(String message, Throwable throwable, Object... args);

	void debugWarn(String message);

	void debugWarn(String message, Object... args);

	void debugWarn(String message, Throwable throwable);

	void debugWarn(String message, Throwable throwable, Object... args);

	void crashReport(CrashReport report);

	@Contract("false, _ -> fail")
	void validateIsTrue(boolean condition, String message);

	String export();

	ControlifyLogger createSubLogger(String name);

	static ControlifyLogger createMasterLogger(org.slf4j.Logger logger) {
		return new ControlifyMasterLogger(logger);
	}
}
