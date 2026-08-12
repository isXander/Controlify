package dev.isxander.controlify.contextual;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.guide.ActionLocation;
import dev.isxander.controlify.contextual.api.Context;
import dev.isxander.controlify.contextual.api.GuideInstance;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.font.BindingFontHelper;
import dev.isxander.controlify.gui.guide.GuideRenderer;
import dev.isxander.controlify.gui.guide.PrecomputedLines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;

import java.util.List;

public class GuideInstanceImpl<C extends Context> implements GuideInstance<C> {
	private final ContextualDomainImpl<C> domain;
	private final RuleEngine<GuideRule.Key, GuideRule> ruleEngine;
	private final Font font;

	public GuideInstanceImpl(
			ContextualDomainImpl<C> domain,
			RuleEngine<GuideRule.Key, GuideRule> ruleEngine,
			Font font
	) {
		this.domain = domain;
		this.ruleEngine = ruleEngine;
		this.font = font;
	}

	private PrecomputedLines leftGuides = PrecomputedLines.EMPTY;
	private PrecomputedLines rightGuides = PrecomputedLines.EMPTY;

	@Override
	public void update(C context) {
		ContextualState state = this.domain.calculateState(context, this.ruleEngine.factDependencies());
		List<GuideRule> guides = this.ruleEngine.evaluate(state);

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
			boolean glyphAfter = font.isBidirectional() ^ (guide.location() == ActionLocation.RIGHT);

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
