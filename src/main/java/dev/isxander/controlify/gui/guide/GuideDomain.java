package dev.isxander.controlify.gui.guide;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.guide.*;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.font.BindingFontHelper;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.Validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class GuideDomain<T extends FactCtx> implements RenderableGuideDomain<T>, SimpleControlifyReloadListener<GuideDomain.Preparations> {
    public static final String DIRECTORY = "guides";
    private static final FileToIdConverter converter = FileToIdConverter.json(DIRECTORY);

    /** The unique identifier of this domain */
    private final Identifier id;

    /** Facts registered via code */
    private final Map<Identifier, Fact<T>> facts = new HashMap<>();
    /** Registered dynamic rules - rules are dynamic if they were created in-code */
    private final List<Rule> dynamicRules = new ArrayList<>();
    /** Whether facts and dynamic rules registries have been frozen */
    private boolean frozen = false;

    /** All rules loaded from resource packs as well as dynamic rules */
    private List<Rule> rules;
    /** The facts that need to be resolved each tick based on references */
    private Map<Identifier, Boolean> resolvedFacts = Map.of();

    private PrecomputedLines leftGuides = PrecomputedLines.EMPTY;
    private PrecomputedLines rightGuides = PrecomputedLines.EMPTY;
    /** True after resource reload since font width/height may be different */
    private boolean precomputeInvalid = false;

    public GuideDomain(Identifier id) {
        this.id = id;
    }

    public boolean updateGuides(T context, Font font) {
        Validate.isTrue(this.frozen, "Cannot update guides before the domain has been frozen");

        if (!this.updateFactResolution(context) && !this.precomputeInvalid) {
            // no facts changed, no need to update guides
            return false;
        }
        this.precomputeInvalid = false;

        var leftBuilder = new PrecomputedLines.Builder();
        var rightBuilder = new PrecomputedLines.Builder();

        var leftConsumed = new HashSet<Identifier>(this.leftGuides.lines().size() + 5);
        var rightConsumed = new HashSet<Identifier>(this.rightGuides.lines().size() + 5);

        ControllerEntity controller = context.controller();
        
        for (Rule rule : this.rules) {
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

        return true;
    }

    /**
     * Updates the fact resolution for all facts in this domain.
     * @param context the context to use for fact resolution
     * @return true if any facts changed, false otherwise
     */
    private boolean updateFactResolution(T context) {
        Validate.isTrue(this.frozen, "Cannot update fact resolution before the domain has been frozen");

        boolean changed = false;
        for (Map.Entry<Identifier, Boolean> entry : this.resolvedFacts.entrySet()) {
            Identifier factId = entry.getKey();
            Fact<T> fact = this.facts.get(factId); // facts.get should never return null here

            boolean newValue = fact.provider().test(context);
            changed |= (newValue != entry.getValue());

            entry.setValue(newValue);
        }

        return changed;
    }

    public Identifier id() {
        return this.id;
    }

    public PrecomputedLines leftGuides() {
        return this.leftGuides;
    }

    public PrecomputedLines rightGuides() {
        return this.rightGuides;
    }

    @Override
    public void registerFact(Fact<? super T> fact) {
        Validate.isTrue(!this.frozen, "Cannot register facts after the domain has been frozen");
        Validate.notNull(fact, "Fact cannot be null");
        Validate.isTrue(!this.facts.containsKey(fact.id()), "Fact with id %s already exists in domain %s", fact.id(), this.id);

        var invariantFact = new Fact<T>(fact.id(), fact.provider()::test);

        this.facts.put(fact.id(), invariantFact);
    }

    @Override
    public void registerDynamicRule(Rule rule) {
        Validate.isTrue(!this.frozen, "Cannot register dynamic rule after the domain has been frozen");
        Validate.notNull(rule, "Rule cannot be null");

        this.dynamicRules.add(rule);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, boolean bottomAligned, boolean textContrast) {
        GuideRenderer.render(graphics, this, Minecraft.getInstance(), bottomAligned, textContrast);
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

    public boolean freeze() {
        if (this.frozen) {
            return false; // already frozen
        }
        this.frozen = true;
        return true;
    }

    public List<Rule> rules() {
        return Collections.unmodifiableList(Validate.notNull(this.rules, "Rules have not been loaded yet for domain %s", this.id));
    }

    @Override
    public CompletableFuture<Preparations> load(ResourceManager manager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            List<Resource> resources = manager.getResourceStack(converter.idToFile(this.id));

            // add all rule sets to a list until we hit a replace = true
            var ruleSets = new ArrayList<RuleSet>();
            for (Resource resource : resources) {
                try (BufferedReader reader = resource.openAsReader()) {
                    JsonElement element = JsonParser.parseReader(reader);
                    DataResult<RuleSet> result = RuleSet.CODEC.parse(JsonOps.INSTANCE, element);
                    var ruleSet = result.getOrThrow();

                    ruleSets.add(ruleSet);
                    if (ruleSet.replace()) {
                        // if this rule set replaces all that's below it, we can skip parsing everything below
                        break;
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to load resource", e);
                }
            }

            // merge all the rules into a single list, where the topmost rules take precedence
            List<Rule> rules = ruleSets.stream()
                    .flatMap(rs -> rs.rules().stream())
                    .toList();

            return new Preparations(rules);
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            Validate.isTrue(this.frozen, "Cannot apply domain before the domain has been frozen");

            int ruleCount = data.rules().size() + this.dynamicRules.size();

            this.resolvedFacts = new HashMap<>(ruleCount * 2);
            for (Rule rule : data.rules()) {
                rule.when().forEach(this::validateFact);
                rule.forbid().forEach(this::validateFact);
            }
            this.rules = new ArrayList<>(ruleCount);
            this.rules.addAll(data.rules());
            this.rules.addAll(this.dynamicRules);

            this.precomputeInvalid = true;
        }, executor);
    }

    private Fact<T> validateFact(Identifier factId) {
        Fact<T> fact = this.facts.get(factId);
        Validate.notNull(fact, "Fact %s is not registered in domain %s", factId, this.id);
        this.resolvedFacts.put(factId, false);
        return fact;
    }

    @Override
    public Identifier getReloadId() {
        return this.id.withPrefix("reload/");
    }

    public record Preparations(
            List<Rule> rules
    ) {}
}
