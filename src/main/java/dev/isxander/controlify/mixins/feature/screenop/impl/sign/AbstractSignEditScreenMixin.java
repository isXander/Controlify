/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.sign;

//? if >=26.4 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.gui.keyboard.InputTarget;
import dev.isxander.controlify.api.gui.keyboard.KeyboardLayouts;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.gui.keyboard.EditBoxInputTarget;
import dev.isxander.controlify.impl.gui.keyboard.KeyboardWidget;
import dev.isxander.controlify.impl.screenop.behaviours.vanilla.SignEditingBehaviour;
import dev.isxander.controlify.mixins.feature.screenop.ScreenAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen implements SignEditingBehaviour.Access, InputTarget {
	@Shadow
	@Final
	protected SignBlockEntity sign;

	@Shadow
	@Final
	private LinearLayout content;

	@Shadow
	protected abstract void onDone();

	@Unique private @Nullable KeyboardWidget keyboard;
	// the line boxes' class is private, so they are collected from the screen's widgets, in line order
	@Unique private final List<EditBox> lines = new ArrayList<>();
	@Unique private int currentLine;

	protected AbstractSignEditScreenMixin(Component title) {
		super(title);
	}

	// Add a screen keyboard if necessary.
	@Inject(method = "init", at = @At("HEAD"))
	private void addKeyboard(CallbackInfo ci) {
		boolean hadKeyboard = this.keyboard != null;
		this.keyboard = null;
		this.lines.clear();
		this.currentLine = 0;

		Controlify.instance().getCurrentController().ifPresent(c -> {
			// if the keyboard is already present, re-add it even if we're in kb/m mode since
			// setting fullscreen will turn it to that mode
			if (!Controlify.instance().currentInputMode().isController() && !hadKeyboard) return;
			if (!c.settings().generic.keyboard.showOnScreenKeyboard) return;

			int keyboardHeight = (int) (this.height * 0.5f);
			this.addRenderableWidget(this.keyboard = new KeyboardWidget(0, this.height - keyboardHeight, this.width, keyboardHeight, Controlify.instance().keyboardLayoutManager().getLayout(KeyboardLayouts.SIMPLE), this));
		});
	}

	// The keyboard takes the title's space.
	@WrapWithCondition(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addTitleHeader(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/Font;)V"))
	private boolean shouldAddTitle(HeaderAndFooterLayout layout, Component title, Font font) {
		return this.keyboard == null;
	}

	// Only add a "Done" button if we aren't doing an on-screen keyboard.
	@WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addToFooter(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
	private LayoutElement shouldAddDoneButton(HeaderAndFooterLayout layout, LayoutElement child, Operation<LayoutElement> original) {
		return this.keyboard == null ? original.call(layout, child) : child;
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void takeLinesOutOfNavigation(CallbackInfo ci) {
		if (this.keyboard == null) return;

		for (GuiEventListener child : List.copyOf(this.children())) {
			if (child instanceof EditBox line) {
				this.lines.add(line);
				// still drawn, but only typed into through the keyboard
				this.removeWidget(line);
				((ScreenAccessor) this).controlify$getRenderables().add(line);
			}
		}
	}

	@WrapOperation(method = "setInitialFocus()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractSignEditScreen;setInitialFocus(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V"))
	private void focusKeyboard(AbstractSignEditScreen screen, GuiEventListener firstLine, Operation<Void> original) {
		if (this.keyboard == null) {
			original.call(screen, firstLine);
			return;
		}

		// like chat, the line keeps its own focus so it draws a cursor, while the screen focuses the keyboard.
		// Focused directly: initial focus navigation finds no path into a container.
		firstLine.setFocused(true);
		this.setFocused(this.keyboard);
	}

	// Move the sign up into the space above the keyboard.
	@ModifyArg(method = "extractBackground", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;"), index = 1)
	private float moveSignAboveKeyboard(float y) {
		return y - this.controlify$keyboardShift();
	}

	@Inject(method = "repositionElements", at = @At("TAIL"))
	private void moveLinesAboveKeyboard(CallbackInfo ci) {
		this.content.setY(this.content.getY() - (int) this.controlify$keyboardShift());
	}

	@Unique private float controlify$keyboardShift() {
		if (this.keyboard == null) return 0;
		// centre the sign in the space above the keyboard instead of the screen
		return (this.height - this.keyboard.getY()) / 2f;
	}

	// A physical keyboard still types into the current line while the screen keyboard is shown.
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void typeIntoCurrentLine(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		EditBox line = this.controlify$currentLine();
		if (line == null) return;

		if (event.isConfirmation()) {
			this.controlify$moveLine(1);
			cir.setReturnValue(true);
		} else if (line.keyPressed(event)) {
			cir.setReturnValue(true);
		}
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		EditBox line = this.controlify$currentLine();
		if (line != null) {
			return line.charTyped(event);
		}
		return super.charTyped(event);
	}

	@Unique private @Nullable EditBox controlify$currentLine() {
		return this.lines.isEmpty() ? null : this.lines.get(this.currentLine);
	}

	@Unique private @Nullable InputTarget controlify$lineTarget() {
		EditBox line = this.controlify$currentLine();
		return line == null ? null : new EditBoxInputTarget(line);
	}

	@Override
	public boolean supportsCharInput() {
		return true;
	}

	@Override
	public boolean acceptChar(char ch, int modifiers) {
		InputTarget target = this.controlify$lineTarget();
		return target != null && target.acceptChar(ch, modifiers);
	}

	@Override
	public boolean supportsKeyCodeInput() {
		return true;
	}

	@Override
	public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
		if (keycode == InputConstants.KEY_RETURN) {
			// like vanilla's Enter
			this.controlify$moveLine(1);
			return true;
		}
		InputTarget target = this.controlify$lineTarget();
		return target != null && target.acceptKeyCode(keycode, scancode, modifiers);
	}

	@Override
	public boolean supportsDone() {
		return true;
	}

	@Override
	public boolean done() {
		this.onDone();
		return true;
	}

	@Override
	public boolean supportsCopying() {
		return true;
	}

	@Override
	public boolean copy() {
		InputTarget target = this.controlify$lineTarget();
		return target != null && target.copy();
	}

	@Override
	public boolean supportsCursorMovement() {
		return true;
	}

	@Override
	public boolean moveCursor(int amount) {
		InputTarget target = this.controlify$lineTarget();
		return target != null && target.moveCursor(amount);
	}

	@Override
	public void controlify$moveLine(int direction) {
		if (this.keyboard != null && !this.lines.isEmpty()) {
			// the keyboard keeps focus; only the line it types into changes
			this.lines.get(this.currentLine).setFocused(false);
			this.currentLine = Math.floorMod(this.currentLine + direction, this.lines.size());
			EditBox next = this.lines.get(this.currentLine);
			next.setFocused(true);
			next.moveCursorToEnd(false);
			return;
		}

		// without the screen keyboard the lines are ordinary widgets, in line order
		List<EditBox> lines = this.children().stream()
				.filter(EditBox.class::isInstance)
				.map(EditBox.class::cast)
				.toList();
		if (lines.isEmpty()) return;

		int current = Math.max(0, lines.indexOf(this.getFocused()));
		EditBox next = lines.get(Math.floorMod(current + direction, lines.size()));
		this.setFocused(next);
		next.moveCursorToEnd(false);
	}

	@Override
	public SignBlockEntity controlify$getSign() {
		return this.sign;
	}

	@Override
	public @Nullable KeyboardWidget controlify$getKeyboard() {
		return this.keyboard;
	}
}
//?} else {
/*import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.gui.keyboard.KeyboardLayouts;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.gui.keyboard.KeyboardWidget;
import dev.isxander.controlify.impl.gui.keyboard.MixinInputTarget;
import dev.isxander.controlify.impl.screenop.behaviours.vanilla.SignEditingBehaviour;
import dev.isxander.controlify.mixins.feature.screenop.ScreenAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen implements SignEditingBehaviour.Access, MixinInputTarget {

	@Shadow
	private @Nullable TextFieldHelper signField;
	@Shadow
	@Final
	private String[] messages;
	@Shadow
	private int line;

	@Shadow
	protected abstract void onDone();

	@Shadow
	@Final
	protected SignBlockEntity sign;

	@Unique protected KeyboardWidget keyboard;

	protected AbstractSignEditScreenMixin(Component title) {
		super(title);
	}

	// Add a screen keyboard if necessary.
	@Inject(method = "init", at = @At("HEAD"))
	private void addKeyboard(CallbackInfo ci) {
		Controlify.instance().getCurrentController().ifPresent(c -> {
			// if the keyboard is already present, re-add it even if we're in kb/m mode since
			// setting fullscreen will turn it to that mode
			if (!Controlify.instance().currentInputMode().isController() && this.keyboard == null) return;
			if (!c.settings().generic.keyboard.showOnScreenKeyboard) return;

			int keyboardHeight = (int) (this.height * 0.5f);
			this.addRenderableWidget(this.keyboard = new KeyboardWidget(0, this.height - keyboardHeight, this.width, keyboardHeight, Controlify.instance().keyboardLayoutManager().getLayout(KeyboardLayouts.SIMPLE), this));
		});
	}

	// Only add a "Done" button if we aren't doing an on-screen keyboard.
	@WrapOperation(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/inventory/AbstractSignEditScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"
			)
	)
	private GuiEventListener shouldAddDoneButton(AbstractSignEditScreen instance, GuiEventListener guiEventListener, Operation<GuiEventListener> original) {
		if (this.keyboard == null) {
			return original.call(instance, guiEventListener);
		} else {
			return null;
		}
	}

	@Definition(id = "centeredText", method = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;centeredText(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V")
	@Definition(id = "title", field = "Lnet/minecraft/client/gui/screens/inventory/AbstractSignEditScreen;title:Lnet/minecraft/network/chat/Component;")
	@Expression("?.centeredText(?, this.title, ?, ?, ?)")
	@WrapWithCondition(method = "extractRenderState", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean preventDrawingTitle(GuiGraphicsExtractor instance, Font font, Component text, int x, int y, int color) {
		return this.keyboard == null;
	}

	@Override
	public boolean controlify$supportsCharInput() {
		return true;
	}

	@Override
	public boolean controlify$acceptChar(char ch, int modifiers) {
		if (this.signField == null) return false;

		return this.signField.charTyped(new CharacterEvent(ch));
	}

	@Override
	public boolean controlify$supportsKeyCodeInput() {
		return true;
	}

	@Override
	public boolean controlify$acceptKeyCode(int keycode, int scancode, int modifiers) {
		if (keycode == InputConstants.KEY_RETURN) {
			this.onDone();
			return true;
		}
		if (this.signField == null) return false;

		return this.signField.keyPressed(dev.isxander.controlify.impl.utils.KeyEvents.of(keycode, scancode, modifiers));
	}

	@Override
	public boolean controlify$supportsCopying() {
		return true;
	}

	@Override
	public boolean controlify$copy() {
		if (this.signField == null) return false;

		minecraft.keyboardHandler.setClipboard(this.messages[this.line]);
		return true;
	}

	@Override
	public boolean controlify$supportsCursorMovement() {
		return true;
	}

	@Override
	public boolean controlify$moveCursor(int amount) {
		if (this.signField == null) return false;

		this.signField.moveByChars(amount);
		return true;
	}

	@Override
	public void controlify$moveLine(int direction) {
		this.line = this.line + direction & 3;
		if (this.signField != null) {
			this.signField.setCursorToEnd();
		}
	}

	@Override
	public SignBlockEntity controlify$getSign() {
		return this.sign;
	}

	@Override
	public KeyboardWidget controlify$getKeyboard() {
		return this.keyboard;
	}
}
*///?}
