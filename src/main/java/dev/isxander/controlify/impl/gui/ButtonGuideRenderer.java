/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui;

import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.font.ActionGlyphs;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.impl.Controlify;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @see dev.isxander.controlify.mixins.feature.guide.screen.AbstractWidgetMixin
 */
public interface ButtonGuideRenderer<T> {
	void controlify$setButtonGuide(RenderData<T> renderData);

	static <T> void registerActionForButton(T button, Supplier<ActionAccessor> action, ButtonGuidePredicate<T> renderPredicate) {
		((ButtonGuideRenderer<T>) button).controlify$setButtonGuide(new RenderData<>(action, renderPredicate));
	}

	record RenderData<T>(Supplier<ActionAccessor> action, ButtonGuidePredicate<T> renderPredicate) {
		public Component getControllerMessage(Action action, Component actualLabel) {
			var component = Component.empty();
			if (!Minecraft.getInstance().font.isBidirectional()) {
				component.append(ActionGlyphs.action(action.spec().id()));
				component.append(CommonComponents.SPACE);
			}
			component.append(actualLabel);
			if (Minecraft.getInstance().font.isBidirectional()) {
				component.append(CommonComponents.SPACE);
				component.append(ActionGlyphs.action(action.spec().id()));
			}
			return component;
		}

		public boolean shouldRender(T renderable) {
			Optional<Action> binding = getAction();
			return binding.isPresent()
					&& Controlify.instance().currentInputMode().isController()
					&& Controlify.instance().getCurrentController().map(c -> c.settings().generic.guide.showScreenGuides).orElse(false)
					&& binding.get().primaryInput() != null
					&& renderPredicate().shouldDisplay(renderable);
		}

		public Optional<Action> getAction() {
			return Controlify.instance().getCurrentController().map(c -> action().get().on(c));
		}
	}
}
