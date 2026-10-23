/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.keyboard;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.font.ActionGlyphs;
import dev.isxander.controlify.api.gui.keyboard.InputTarget;
import dev.isxander.controlify.api.screenop.ElementBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.HoldRepeatHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

/**
 * Represents a single key widget within a {@link KeyboardWidget}.
 * It handles rendering, input processing, and interaction with the keyboard layout.
 */
public class KeyWidget extends AbstractWidget implements ElementBehaviour {
	public static final Identifier SPRITE = CUtil.rl("keyboard/key");
	public static final Identifier SPRITE_PRESSED = CUtil.rl("keyboard/key_pressed");

	private final KeyboardWidget keyboard;
	private final KeyboardLayout.Key key;

	private final Component regularLabel, shiftedLabel;
	private final boolean supportsRegular, supportsShifted;

	private boolean mousePressed;
	private boolean buttonPressed;
	private boolean shortcutPressed;
	private boolean ignoreHeldPress;
	private boolean newlyFocused = true;
	private final HoldRepeatHelper focusedRepeatHelper = new HoldRepeatHelper(10, 2);
	private final HoldRepeatHelper shortcutRepeatHelper = new HoldRepeatHelper(10, 2);

	private final int renderScale;
	private final int renderWidth, renderHeight;

	public KeyWidget(int x, int y, int width, int height, int renderScale, KeyboardLayout.Key key, KeyboardWidget keyboard) {
		super(x, y, width, height, Component.literal("Key"));
		this.keyboard = keyboard;
		this.key = key;

		this.regularLabel = createLabel(key, false);
		this.shiftedLabel = createLabel(key, true);
		this.supportsRegular = supportsAction(false);
		this.supportsShifted = supportsAction(true);

		this.renderScale = renderScale;
		this.renderWidth = width / renderScale;
		this.renderHeight = height / renderScale;
	}

	public void extractKeyBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		this.active = this.supportsAction();
		if (!this.isFocused()) {
			this.buttonPressed = false;
		}

		doScaledExtraction(graphics, () -> {
			Identifier sprite = isVisuallyPressed() ? SPRITE_PRESSED : SPRITE;
			int x = getX() + 1;
			int y = getY() + 1;
			graphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					sprite,
					x, y,
					renderWidth - 2, renderHeight - 2
			);

			if (isHoveredOrFocused()) {
				graphics.outline(getX() - 1, getY() - 1, renderWidth + 2, renderHeight + 2, 0x80FFFFFF);
			}

			if (!this.active) {
				// gray out the key if it does not support the action
				graphics.fill(getX() + 1, getY() + 1, getX() + renderWidth - 1, getY() + renderHeight - 1, 0x30000000);
			}
		});
	}

	public void extractKeyForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTick) {
		doScaledExtraction(graphics, () -> {
			Component label = this.keyboard.isShifted() ? this.shiftedLabel : this.regularLabel;
			graphics.centeredText(
					Minecraft.getInstance().font,
					label,
					getX() + renderWidth / 2,
					getY() + renderHeight / 2 - 4 + (isVisuallyPressed() ? 2 : 0),
					0xFFFFFFFF
			);
		});
	}

	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		// custom rendered above
	}

	@Override
	public void onFocusChanged(@NonNull ScreenContext<?> screen, boolean focused) {
		this.newlyFocused = focused;
		this.buttonPressed = false;
		this.focusedRepeatHelper.reset();
	}

	@Override
	public void handleInteraction(@NonNull ScreenContext<?> screen, @NonNull Controller controller, @NonNull Runnable next) {
		if (screen.isVirtualMouseEnabled()) {
			next.run();
			return;
		}
		Action guiPress = ControlifyActions.GUI_PRESS.on(controller);
		boolean held = guiPress.latch().isLatchActive();
		if (this.newlyFocused) {
			this.ignoreHeldPress = held;
			this.newlyFocused = false;
		}
		if (!held) {
			this.ignoreHeldPress = false;
		}
		this.buttonPressed = held && !this.ignoreHeldPress;
		if (this.focusedRepeatHelper.shouldAction(this.buttonPressed)) {
			onPress();
		}
		guiPress.pulse().consumeAllPulses();
		next.run();
	}

	public void handleShortcut(Controller controller) {
		this.key.shortcutBinding().ifPresent(accessor -> {
			Action shortcut = accessor.on(controller);
			this.shortcutPressed = shortcut.latch().isLatchActive();
			boolean repeat = this.shortcutRepeatHelper.shouldAction(this.shortcutPressed);
			boolean pressed = shortcut.pulse().consumeAllPulses();
			if (pressed || repeat) {
				onPress();
			}
		});
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
		double mouseX = mouseButtonEvent.x();
		double mouseY = mouseButtonEvent.y();
		if (isMouseOver(mouseX, mouseY)) {
			this.mousePressed = true;
			onPress();
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(@NonNull MouseButtonEvent mouseButtonEvent) {
		this.mousePressed = false;
		return super.mouseReleased(mouseButtonEvent);
	}

	private void onPress() {
		KeyboardLayout.KeyFunction keyFunction = this.getKeyFunction();
		InputTarget inputConsumer = this.keyboard.getInputTarget();

		playDownSound(Minecraft.getInstance().getSoundManager());

		boolean wasShiftAction = false;

		switch (keyFunction) {
			case KeyboardLayout.KeyFunction.StringFunc stringKey -> insertText(stringKey.string(), inputConsumer);

			case KeyboardLayout.KeyFunction.CodeFunc codeKey ->
					codeKey.codes().forEach(code -> inputConsumer.acceptKeyCode(code.keycode(), code.scancode(), code.modifier()));

			case KeyboardLayout.KeyFunction.SpecialFunc specialKey -> {
				switch (specialKey.action()) {
					case SHIFT -> {
						toggleShift();
						wasShiftAction = true;
					}
					case SHIFT_LOCK -> {
						toggleShiftLock();
						wasShiftAction = true;
					}

					case ENTER -> inputConsumer.acceptKeyCode(InputConstants.KEY_RETURN, 0, 0);
					case BACKSPACE -> inputConsumer.acceptKeyCode(InputConstants.KEY_BACKSPACE, 0, 0);
					case TAB -> inputConsumer.acceptKeyCode(InputConstants.KEY_TAB, 0, 0);
					case LEFT_ARROW -> inputConsumer.acceptKeyCode(InputConstants.KEY_LEFT, 0, 0);
					case RIGHT_ARROW -> inputConsumer.acceptKeyCode(InputConstants.KEY_RIGHT, 0, 0);
					case UP_ARROW -> inputConsumer.acceptKeyCode(InputConstants.KEY_UP, 0, 0);
					case DOWN_ARROW -> inputConsumer.acceptKeyCode(InputConstants.KEY_DOWN, 0, 0);

					case PASTE -> {
						String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
						insertText(clipboard, inputConsumer);
					}
					case COPY_ALL -> {
						inputConsumer.copy();
					}

					case PREVIOUS_LAYOUT -> {
						this.keyboard.getPreviousLayoutId().ifPresent(this::changeLayout);
					}

					default -> {
						CUtil.LOGGER.warn("Unhandled code action: " + specialKey.action());
					}
				}
			}

			case KeyboardLayout.KeyFunction.ChangeLayoutFunc func -> this.changeLayout(func.layout());
		}

		if (!wasShiftAction && this.keyboard.isShifted() && !this.keyboard.isShiftLocked()) {
			// the key is shiftable if the key identity is different (i.e. it's a different key when shifted)
			if (this.key.regular() != this.key.shifted()) {
				this.keyboard.setShifted(false);
			}
		}
	}

	protected void toggleShift() {
		if (!this.keyboard.isShiftLocked()) {
			this.keyboard.setShifted(!this.keyboard.isShifted());
		} else {
			this.keyboard.setShifted(false);
			this.keyboard.setShiftLocked(false);
		}
	}

	protected void toggleShiftLock() {
		boolean shiftLocked = !this.keyboard.isShiftLocked();
		this.keyboard.setShiftLocked(shiftLocked);
		this.keyboard.setShifted(shiftLocked);
	}

	public KeyboardLayout.Key getKey() {
		return this.key;
	}

	public KeyboardLayout.KeyFunction getKeyFunction() {
		return this.key.getFunction(this.keyboard.isShifted());
	}

	public boolean isVisuallyPressed() {
		return this.mousePressed
			|| this.buttonPressed
			|| this.shortcutPressed
			|| this.isShiftKeyAndShifting()
			|| this.isShiftLockKeyAndShiftLocked();
	}

	private boolean isShiftKeyAndShifting() {
		return this.keyboard.isShifted() && !this.keyboard.isShiftLocked()
			&& this.getKeyFunction() instanceof KeyboardLayout.KeyFunction.SpecialFunc(
				KeyboardLayout.KeyFunction.SpecialFunc.Action action
		)
			&& action == KeyboardLayout.KeyFunction.SpecialFunc.Action.SHIFT;
	}

	private boolean isShiftLockKeyAndShiftLocked() {
		return this.keyboard.isShiftLocked()
			&& this.getKeyFunction() instanceof KeyboardLayout.KeyFunction.SpecialFunc(
				KeyboardLayout.KeyFunction.SpecialFunc.Action action
		)
			&& action == KeyboardLayout.KeyFunction.SpecialFunc.Action.SHIFT_LOCK;
	}

	private boolean supportsAction(boolean shifted) {
		boolean supportsCharInput = this.keyboard.getInputTarget().supportsCharInput();
		boolean supportsKeyCodeInput = this.keyboard.getInputTarget().supportsKeyCodeInput();
		boolean supportsCopying = this.keyboard.getInputTarget().supportsCopying();

		return switch (this.getKey().getFunction(shifted)) {
			case KeyboardLayout.KeyFunction.StringFunc ignored -> supportsCharInput;
			case KeyboardLayout.KeyFunction.CodeFunc ignored -> supportsKeyCodeInput;
			case KeyboardLayout.KeyFunction.SpecialFunc specialFunc -> switch (specialFunc.action()) {
				case ENTER, BACKSPACE, LEFT_ARROW, RIGHT_ARROW, UP_ARROW, DOWN_ARROW -> supportsKeyCodeInput;
				case TAB, PASTE -> supportsCharInput;
				case COPY_ALL -> supportsCopying;
				case SHIFT, SHIFT_LOCK, PREVIOUS_LAYOUT -> true;
			};
			case KeyboardLayout.KeyFunction.ChangeLayoutFunc ignored -> true;
		};
	}

	private boolean supportsAction() {
		return this.keyboard.isShifted() ? this.supportsShifted : this.supportsRegular;
	}

	private void changeLayout(Identifier layoutId) {
		KeyboardLayoutWithId layoutWithId = Controlify.instance().keyboardLayoutManager().getLayout(layoutId);
		this.keyboard.updateLayout(layoutWithId);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	private void doScaledExtraction(GuiGraphicsExtractor graphics, Runnable runnable) {
		var pose = graphics.pose().pushMatrix();
		pose.translate(getX(), getY());
		pose.scale(this.renderScale, this.renderScale);
		pose.translate(-getX(), -getY());

		runnable.run();

		pose.popMatrix();
	}

	private static void insertText(String text, InputTarget inputConsumer) {
		// One `char` is not necessarily one visible character.
		// Some characters, such as emojis, are represented using surrogate pairs,
		// meaning they span two `char`s.
		// Code points are used to represent characters that *may* be represented by surrogate pairs.
		text.codePoints().forEach((codePoint) -> {
			// guess the modifier based on the nature of the character
			int modCapital = Character.isUpperCase(codePoint) ? InputConstants.MOD_SHIFT : 0;
			int modifiers = modCapital;

			if (Character.isBmpCodePoint(codePoint)) {
				inputConsumer.acceptChar((char) codePoint, modifiers);
			} else if (Character.isValidCodePoint(codePoint)) {
				inputConsumer.acceptChar(Character.highSurrogate(codePoint), modifiers);
				inputConsumer.acceptChar(Character.lowSurrogate(codePoint), modifiers);
			}
		});
	}

	private static Component createLabel(KeyboardLayout.Key key, boolean shift) {
		KeyboardLayout.KeyFunction keyFunction = key.getFunction(shift);

		return key.shortcutBinding()
				.map(b -> ActionGlyphs.action(b.actionId()))
				.<Component>map(glyph -> Component.empty()
						.append(glyph)
						.append(" ")
						.append(keyFunction.displayName()))
				.orElseGet(keyFunction::displayName);
	}
}
