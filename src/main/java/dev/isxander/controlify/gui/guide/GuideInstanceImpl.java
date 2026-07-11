package dev.isxander.controlify.gui.guide;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.guide.*;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.font.BindingFontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class GuideInstanceImpl<T extends FactCtx> implements GuideInstance<T> {
    private final GuideDomainImpl<T> domain;

    /** The facts that need to be resolved each tick based on references */
    private Map<Identifier, Boolean> resolvedFacts = Map.of();

    private PrecomputedLines leftGuides = PrecomputedLines.EMPTY;
    private PrecomputedLines rightGuides = PrecomputedLines.EMPTY;

    /** True after reset since font width/height may be different */
    private boolean precomputeInvalid = false;
    private int reloadEpoch = 0;

    public GuideInstanceImpl(GuideDomainImpl<T> domain) {
        Validate.isTrue(domain.isFrozen(), "Cannot create Guide instance until domain has been frozen");
        this.domain = domain;
        this.reset();
    }

    @Override
    public boolean update(T context, Font font) {
        if (this.domain.currentReloadEpoch() != this.reloadEpoch) {
            reset();
        }

        if (!this.updateFactResolution(context) && !this.precomputeInvalid) {
            // no facts changed, no need to update guides
            return false;
        }
        this.precomputeInvalid = false;

        precomputeGuideLayout(context, font);

        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, boolean bottomAligned, boolean textContrast) {
        GuideRenderer.extractRenderState(graphics, this, Minecraft.getInstance(), bottomAligned, textContrast);
    }

    @Override
    public Renderable renderable(boolean bottomAligned, boolean textContrast) {
        return new GuideRenderer.Renderable(
                this,
                Minecraft.getInstance(),
                bottomAligned,
                textContrast
        );
    }

    /**
     * Updates the fact resolution for all facts in this domain.
     * @param context the context to use for fact resolution
     * @return true if any facts changed, false otherwise
     */
    private boolean updateFactResolution(T context) {
        boolean changed = false;
        for (Map.Entry<Identifier, Boolean> entry : this.resolvedFacts.entrySet()) {
            Identifier factId = entry.getKey();
            Fact<T> fact = this.domain.facts().get(factId); // facts.get should never return null here

            boolean newValue = fact.provider().test(context);
            changed |= (newValue != entry.getValue());

            entry.setValue(newValue);
        }

        return changed;
    }

    private void precomputeGuideLayout(T context, Font font) {
        var leftBuilder = new PrecomputedLines.Builder();
        var rightBuilder = new PrecomputedLines.Builder();

        var leftConsumed = new HashSet<Identifier>(this.leftGuides.lines().size() + 5);
        var rightConsumed = new HashSet<Identifier>(this.rightGuides.lines().size() + 5);

        ControllerEntity controller = context.controller();

        for (Rule rule : this.domain.rules()) {
            InputBinding binding = rule.binding().onOrNull(controller);
            if (binding == null || binding.isUnbound()) {
                // skip this rule
                continue;
            }

            // get the builder and consumed set based on the rule's location
            PrecomputedLines.Builder builder;
            Set<Identifier> consumedBinds;
            switch (rule.where()) {
                case LEFT -> {
                    builder = leftBuilder;
                    consumedBinds = leftConsumed;
                }
                case RIGHT -> {
                    builder = rightBuilder;
                    consumedBinds = rightConsumed;
                }
                default -> throw new IllegalStateException("Unexpected action location: " + rule.where());
            }

            // add the binding to the consumed set so we skip extra work for the same binding
            if (consumedBinds.contains(binding.id())) {
                continue;
            }

            // check if the rule's conditions are met
            boolean whenPermits = rule.when().stream()
                    .allMatch(this.resolvedFacts::get);
            boolean forbidPermits = rule.forbid().stream()
                    .noneMatch(this.resolvedFacts::get);

            if (whenPermits && forbidPermits) {
                consumedBinds.add(binding.id());

                // put the glyph after or before the binding glyph based on the rule's location and font direction
                boolean glyphAfter = font.isBidirectional() ^ (rule.where() == ActionLocation.RIGHT);

                // formulate the text to display
                Component text = Component.empty()
                        .append(glyphAfter ? rule.then() : binding.inputGlyph())
                        .append(" ")
                        .append(glyphAfter ? binding.inputGlyph() : rule.then());

                // precompute the width and height of the text
                int ruleNameWidth = font.width(rule.then());
                int width = font.width(text);
                int glyphWidth = width - ruleNameWidth;
                int backgroundLeft = glyphAfter ? 0 : glyphWidth;
                int backgroundRight = backgroundLeft + ruleNameWidth;

                int height = BindingFontHelper.getComponentHeight(font, binding.inputGlyph()); // use input glyph height only as it's bound to be the tallest

                builder.addLine(text, width, height, backgroundLeft, backgroundRight);
            }
        }
        this.leftGuides = leftBuilder.build();
        this.rightGuides = rightBuilder.build();
    }

    /**
     * When the reload epoch of the backing domain changes,
     * this method is called which ensures all state is reset.
     */
    private void reset() {
        this.resolvedFacts = new HashMap<>(this.domain.rules().size() * 2);

        Stream<Identifier> when = this.domain.rules().stream()
                .flatMap(rule -> rule.when().stream());
        Stream<Identifier> forbid = this.domain.rules().stream()
                .flatMap(rule -> rule.forbid().stream());
        Stream.concat(when, forbid)
                .distinct()
                .forEach(factId -> {
                    Fact<T> fact = this.domain.facts().get(factId);
                    Validate.notNull(fact, "Fact %s is not registered in domain %s", factId, this.domain.id());
                    this.resolvedFacts.put(factId, false);
                });

        this.reloadEpoch = this.domain.currentReloadEpoch();
        this.precomputeInvalid = true;
    }

    public PrecomputedLines leftGuides() {
        return this.leftGuides;
    }

    public PrecomputedLines rightGuides() {
        return this.rightGuides;
    }

    @Override
    public GuideDomain<T> domain() {
        return this.domain;
    }
}
