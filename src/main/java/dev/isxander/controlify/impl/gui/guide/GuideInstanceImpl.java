/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.guide;

import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.contextual.Context;
import dev.isxander.controlify.api.contextual.GuideInstance;
import dev.isxander.controlify.api.font.ActionGlyphs;
import dev.isxander.controlify.impl.contextual.ContextualDomainImpl;
import dev.isxander.controlify.impl.contextual.ContextualState;
import dev.isxander.controlify.impl.contextual.GuideLocation;
import dev.isxander.controlify.impl.contextual.GuideRule;
import dev.isxander.controlify.impl.contextual.RuleEngine;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.font.ComponentFontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Supplier;

public class GuideInstanceImpl<C extends Context> implements GuideInstance<C> {
	private final ContextualDomainImpl<C> domain;
	private final Supplier<RuleEngine<GuideRule.Key, GuideRule>> ruleEngineSupplier;
	private final Font font;

	private PrecomputedLines leftGuides = PrecomputedLines.EMPTY;
	private PrecomputedLines rightGuides = PrecomputedLines.EMPTY;

	public GuideInstanceImpl(
			ContextualDomainImpl<C> domain,
			Supplier<RuleEngine<GuideRule.Key, GuideRule>> ruleEngineSupplier,
			Font font
	) {
		this.domain = domain;
		this.ruleEngineSupplier = ruleEngineSupplier;
		this.font = font;
	}

	@Override
	public void update(@NonNull C context) {
		RuleEngine<GuideRule.Key, GuideRule> ruleEngine = this.ruleEngineSupplier.get();
		ContextualState state = this.domain.calculateState(context, ruleEngine.factDependencies());
		List<GuideRule> guides = ruleEngine.evaluate(state, _ -> true, "guide/" + this.domain.id());

		var leftBuilder = new PrecomputedLines.Builder();
		var rightBuilder = new PrecomputedLines.Builder();

		var controller = (ControllerImpl) context.controller();

		for (GuideRule guide : guides) {
			Action action = guide.action().onOrNull(controller);
			if (action == null || action.boundInput() == null) {
				// skip this rule if the action is not bound
				continue;
			}

			PrecomputedLines.Builder builder = switch (guide.location()) {
				case LEFT -> leftBuilder;
				case RIGHT -> rightBuilder;
			};

			// put the glyph after or before the action glyph based on the rule's location and font direction
			boolean glyphAfter = font.isBidirectional() ^ (guide.location() == GuideLocation.RIGHT);

			// formulate the text to display
			Component text = Component.empty()
					.append(glyphAfter ? guide.text() : ActionGlyphs.action(action.spec().id()))
					.append(" ")
					.append(glyphAfter ? ActionGlyphs.action(action.spec().id()) : guide.text());

			// precompute the width and height of the text
			int ruleNameWidth = font.width(guide.text());
			int width = font.width(text);
			int glyphWidth = width - ruleNameWidth;
			int backgroundLeft = glyphAfter ? 0 : glyphWidth;
			int backgroundRight = backgroundLeft + ruleNameWidth;

			int height = ComponentFontHelper.getComponentHeight(font, ActionGlyphs.action(action.spec().id())); // use input glyph height only as it's bound to be the tallest

			builder.addLine(text, width, height, backgroundLeft, backgroundRight);
		}
		this.leftGuides = leftBuilder.build();
		this.rightGuides = rightBuilder.build();
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, boolean bottomAligned, boolean textContrast, int guiScale) {
		GuideRenderer.extractRenderState(
				graphics,
				this,
				Minecraft.getInstance(),
				bottomAligned,
				textContrast,
				guiScale
		);
	}

	@Override
	public Renderable renderable(boolean bottomAligned, boolean textContrast, int guiScale) {
		return new GuideRenderer.Renderable(
				this,
				Minecraft.getInstance(),
				bottomAligned,
				textContrast,
				guiScale
		);
	}

	public PrecomputedLines leftGuides() {
		return this.leftGuides;
	}

	public PrecomputedLines rightGuides() {
		return this.rightGuides;
	}
}
