package dev.isxander.controlify.impl.gui;

import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.gui.ButtonGuideApi;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;

import java.util.function.Supplier;

public final class ButtonGuideApiImpl implements ButtonGuideApi {
	public static final ButtonGuideApi INSTANCE = new ButtonGuideApiImpl();

	private ButtonGuideApiImpl() {
	}

	@Override
	public <T> void addGuideToButton(T button, ActionAccessor action, ButtonGuidePredicate<T> renderPredicate) {
		addGuideToButton(button, () -> action, renderPredicate);
	}

	@Override
	public <T> void addGuideToButton(T button, Supplier<ActionAccessor> action, ButtonGuidePredicate<T> renderPredicate) {
		ButtonGuideRenderer.registerActionForButton(button, action, renderPredicate);
	}
}
