/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record TrackingConsumer(Consumer<Long> start, BiConsumer<Long, Long> progressConsumer, Consumer<Optional<Throwable>> onComplete) {
}
