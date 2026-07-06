package dev.isxander.controlify.gui.guide;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.api.guide.*;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
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

public class GuideDomainImpl<T extends FactCtx> implements GuideDomain<T>, SimpleControlifyReloadListener<GuideDomainImpl.Preparations> {
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
    /** Incremented on reload, so instances can check when they need to reset. */
    private int reloadEpoch;

    public GuideDomainImpl(Identifier id) {
        this.id = id;
    }

    public Identifier id() {
        return this.id;
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

            this.rules = new ArrayList<>(ruleCount);
            this.rules.addAll(data.rules());
            this.rules.addAll(this.dynamicRules);

            this.reloadEpoch += 1;
        }, executor);
    }

    public boolean freeze() {
        if (this.frozen) {
            return false; // already frozen
        }
        this.frozen = true;
        return true;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public List<Rule> rules() {
        return Collections.unmodifiableList(Validate.notNull(this.rules, "Rules have not been loaded yet for domain %s", this.id));
    }

    public Map<Identifier, Fact<T>> facts() {
        return Collections.unmodifiableMap(Validate.notNull(this.facts, "Facts have not been loaded yet for domain %s", this.id));
    }

    public int currentReloadEpoch() {
        return this.reloadEpoch;
    }

    @Override
    public Identifier getReloadId() {
        return this.id.withPrefix("reload/");
    }

    @Override
    public GuideInstance<T> createInstance() {
        return new GuideInstanceImpl<>(this);
    }

    public record Preparations(
            List<Rule> rules
    ) {}
}
