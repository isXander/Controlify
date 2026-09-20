/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gui.guide;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.contextual.GuideLocation;
import dev.isxander.controlify.api.contextual.Context;
import dev.isxander.controlify.api.contextual.GuideInstance;
import dev.isxander.controlify.contextual.ContextualDomainImpl;
import dev.isxander.controlify.contextual.ContextualState;
import dev.isxander.controlify.contextual.GuideRule;
import dev.isxander.controlify.contextual.RuleEngine;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.font.BindingFontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;

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
	public void update(C context) {
		RuleEngine<GuideRule.Key, GuideRule> ruleEngine = this.ruleEngineSupplier.get();
		ContextualState state = this.domain.calculateState(context, ruleEngine.factDependencies());
		List<GuideRule> guides = ruleEngine.evaluate(state, _ -> true, "guide/" + this.domain.id());

		var leftBuilder = new PrecomputedLines.Builder();
		var rightBuilder = new PrecomputedLines.Builder();

		ControllerEntity controller = context.controller();

		for (GuideRule guide : guides) {
			InputBinding binding = guide.binding().onOrNull(controller);
			if (binding == null || binding.isUnbound()) {
				// skip this rule if the binding is not bound
				continue;
			}

			PrecomputedLines.Builder builder = switch (guide.location()) {
				case LEFT -> leftBuilder;
				case RIGHT -> rightBuilder;
			};

			// put the glyph after or before the binding glyph based on the rule's location and font direction
			boolean glyphAfter = font.isBidirectional() ^ (guide.location() == GuideLocation.RIGHT);

			// formulate the text to display
			Component text = Component.empty()
					.append(glyphAfter ? guide.text() : binding.inputGlyph())
					.append(" ")
					.append(glyphAfter ? binding.inputGlyph() : guide.text());

			// precompute the width and height of the text
			int ruleNameWidth = font.width(guide.text());
			int width = font.width(text);
			int glyphWidth = width - ruleNameWidth;
			int backgroundLeft = glyphAfter ? 0 : glyphWidth;
			int backgroundRight = backgroundLeft + ruleNameWidth;

			int height = BindingFontHelper.getComponentHeight(font, binding.inputGlyph()); // use input glyph height only as it's bound to be the tallest

			builder.addLine(text, width, height, backgroundLeft, backgroundRight);
		}
		this.leftGuides = leftBuilder.build();
		this.rightGuides = rightBuilder.build();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, boolean bottomAligned, boolean textContrast, int guiScale) {
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
