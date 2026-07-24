/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.haptic;

import javax.sound.sampled.AudioFormat;

public record CompleteSoundData(byte[] audio, AudioFormat format) {
}
