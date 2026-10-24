/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.font;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.mutable.MutableInt;

public final class ComponentFontHelper {
	private ComponentFontHelper() {
	}

	public static int getComponentHeight(Font font, FormattedCharSequence text) {
		MutableInt mutableInt = new MutableInt();
		text.accept((index, style, codePoint) -> {
			mutableInt.setValue(Math.max(mutableInt.intValue(), getHeight(font, codePoint, style)));
			return true;
		});
		return mutableInt.intValue();
	}

	public static int getComponentHeight(Font font, FormattedText text) {
		MutableInt mutableInt = new MutableInt();
		StringDecomposer.iterateFormatted(text, Style.EMPTY, (index, style, codePoint) -> {
			mutableInt.setValue(Math.max(mutableInt.intValue(), getHeight(font, codePoint, style)));
			return true;
		});
		return mutableInt.intValue();
	}

	private static int getHeight(Font font, int codepoint, Style style) {
		// i can't figure out how to get the height here
		return 15;
	}
}
