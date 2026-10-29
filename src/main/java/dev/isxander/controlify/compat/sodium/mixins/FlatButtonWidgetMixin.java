/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compat.sodium.mixins;

//? if sodium {

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.impl.gui.ButtonGuideRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.Optional;

@Mixin(FlatButtonWidget.class)
public class FlatButtonWidgetMixin implements ButtonGuideRenderer<FlatButtonWidget> {
	@Shadow
	private boolean enabled;
	@Shadow
	private boolean visible;

	@Unique private RenderData<FlatButtonWidget> renderData = null;
	@Unique private final Map<Action, Component> controllerMessages = new Object2ObjectArrayMap<>(2);

	@WrapMethod(method = "getRenderedLabel")
	private Component modifyRenderedLabel(Operation<Component> original) {
		return getControllerMessage(original.call());
	}

	@Unique private Component getControllerMessage(Component actualLabel) {
		if (!shouldRender()) {
			return actualLabel;
		}

		return getBind().map(bind -> controllerMessages
				.computeIfAbsent(bind, b -> renderData.getControllerMessage(b, actualLabel)))
				.orElse(actualLabel);
	}

	@Unique protected boolean shouldRender() {
		return renderData != null
				&& enabled
				&& visible
				&& renderData.shouldRender((FlatButtonWidget) (Object) this);
	}

	@Unique private Optional<Action> getBind() {
		if (renderData == null) {
			return Optional.empty();
		}
		return renderData.getAction();
	}

	@Override
	public void controlify$setButtonGuide(RenderData<FlatButtonWidget> renderData) {
		this.renderData = renderData;
		this.controllerMessages.clear();
	}
}
//?}
