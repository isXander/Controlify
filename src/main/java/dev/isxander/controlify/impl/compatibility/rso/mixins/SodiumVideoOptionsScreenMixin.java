/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.rso.mixins;

//? if reeses_sodium_options {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.compatibility.sodium.screenop.SodiumScreenOperations;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import me.flashyreese.mods.reeses_sodium_options.client.gui.SodiumVideoOptionsScreen;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.AbstractFrame;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.tab.Tab;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.tab.TabFrame;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(SodiumVideoOptionsScreen.class)
public abstract class SodiumVideoOptionsScreenMixin extends Screen implements SodiumScreenOperations {
	@Shadow
	private FlatButtonWidget applyButton;
	@Shadow
	private FlatButtonWidget closeButton;
	@Shadow
	private FlatButtonWidget undoButton;

	@Unique private TabFrame tabFrame;

	protected SodiumVideoOptionsScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void notifyProcessorRebuild(CallbackInfo ci) {
		focusOnFirstControl();
		ScreenContextImpl.get(this).onWidgetRebuild();
	}

	@ModifyExpressionValue(
			method = "lambda$parentBasicFrameBuilder$9",
			at = @At(
					value = "INVOKE",
					target = "Lme/flashyreese/mods/reeses_sodium_options/client/gui/frame/tab/TabFrame$Builder;build()Lme/flashyreese/mods/reeses_sodium_options/client/gui/frame/tab/TabFrame;"
			)
	)
	private TabFrame storeBuiltTabFrame(TabFrame original) {
		return tabFrame = original;
	}

	@ModifyArg(
			method = "lambda$parentBasicFrameBuilder$9",
			at = @At(
					value = "INVOKE",
					target = "Lme/flashyreese/mods/reeses_sodium_options/client/gui/frame/tab/TabFrame$Builder;onSetTab(Ljava/lang/Runnable;)Lme/flashyreese/mods/reeses_sodium_options/client/gui/frame/tab/TabFrame$Builder;"
			)
	)
	private Runnable setInitialFocusOnTabChange(Runnable onSetTab) {
		return () -> {
			onSetTab.run();
			Minecraft.getInstance().schedule(this::focusOnFirstControl);
		};
	}

	@Unique private void focusOnFirstControl() {
		AbstractFrame tabContentsFrame = ((TabFrameAccessor) tabFrame).getSelectedFrame();
		List<ControlElement<?>> controlElements = ((AbstractFrameAccessor) tabContentsFrame).getControlElements();
		if (!controlElements.isEmpty() && !ScreenContextImpl.get(this).virtualMouse().isRequired()) {
			setInitialFocus(controlElements.get(0));
		}
	}

	@Override
	public void controlify$nextPage() {
		var accessor = (TabFrameAccessor) this.tabFrame;
		List<Tab<?>> tabs = accessor.getTabs();
		Optional<Tab<?>> selectedTab = getSelectedTab(accessor);
		if (selectedTab.isEmpty()) return;

		var currentIndex = tabs.indexOf(selectedTab.get());
		var nextIndex = (currentIndex + 1) % tabs.size();

		tabFrame.setTab(Optional.of(tabs.get(nextIndex)));
	}

	@Override
	public void controlify$prevPage() {
		var accessor = (TabFrameAccessor) this.tabFrame;
		List<Tab<?>> tabs = accessor.getTabs();
		Optional<Tab<?>> selectedTab = getSelectedTab(accessor);
		if (selectedTab.isEmpty()) return;

		var currentIndex = tabs.indexOf(selectedTab.get());
		var nextIndex = (currentIndex - 1 + tabs.size()) % tabs.size();

		tabFrame.setTab(Optional.of(tabs.get(nextIndex)));
	}

	@Override
	public FlatButtonWidget controlify$getApplyButton() {
		return applyButton;
	}

	@Override
	public FlatButtonWidget controlify$getCloseButton() {
		return closeButton;
	}

	@Override
	public FlatButtonWidget controlify$getUndoButton() {
		return undoButton;
	}

	@Unique private Optional<Tab<?>> getSelectedTab(TabFrameAccessor accessor) {
		return accessor.getSelectedTab();
	}
}
*///?}
