/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record LogMessage(String message, Object[] args, Throwable throwable, boolean debug, LogLevel level, LocalTime time) {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	public LogMessage(String message, Object[] args, Throwable throwable, boolean debug, LogLevel level) {
		this(message, args, throwable, debug, level, LocalTime.now());
	}

	public StringBuilder append(StringBuilder stringBuilder) {
		// format {} placeholders
		String expandedString = message;
		for (Object arg : args) {
			expandedString = expandedString.replaceFirst("\\{}", String.valueOf(arg));
		}

		return stringBuilder
				.append('[').append(formatter.format(LocalTime.now())).append(']')
				.append(' ')
				.append(level == LogLevel.ERROR ? "[ERROR]" : level == LogLevel.WARN ? "[WARN]" : "")
				.append(' ')
				.append(expandedString)
				.append(throwable != null ? "\n" + getStacktrace(throwable) : "")
				.append("\n");
	}

	private static String getStacktrace(Throwable throwable) {
		StringWriter writer = new StringWriter();
		throwable.printStackTrace(new PrintWriter(writer, true));
		return writer.toString();
	}
}
