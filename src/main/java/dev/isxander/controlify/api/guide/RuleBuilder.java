package dev.isxander.controlify.api.guide;

import dev.isxander.controlify.api.bind.InputBindingSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class RuleBuilder {
    private InputBindingSupplier binding;
    private ActionLocation where;
    private final Set<ResourceLocation> when = new HashSet<>();
    private final Set<ResourceLocation> forbid = new HashSet<>();
    private Component then;

    RuleBuilder() {}

    public RuleBuilder binding(InputBindingSupplier binding) {
        this.binding = binding;
        return this;
    }

    public RuleBuilder where(ActionLocation where) {
        this.where = where;
        return this;
    }

    public RuleBuilder when(ResourceLocation... when) {
        this.when.addAll(Arrays.asList(when));
        return this;
    }

    @SafeVarargs
    public final <T extends FactCtx> StrictRuleBuilder<T> when(Fact<T>... when) {
        return new StrictRuleBuilder<T>().when(when);
    }

    public RuleBuilder forbid(ResourceLocation... forbid) {
        this.forbid.addAll(Arrays.asList(forbid));
        return this;
    }

    @SafeVarargs
    public final <T extends FactCtx> StrictRuleBuilder<T> forbid(Fact<T>... forbid) {
        return this.new StrictRuleBuilder<T>().forbid(forbid);
    }

    public RuleBuilder then(Component then) {
        this.then = then;
        return this;
    }

    public <T extends FactCtx> StrictRuleBuilder<T> strict() {
        return this.new StrictRuleBuilder<>();
    }

    public Rule build() {
        Validate.notNull(binding, "`binding` must not be null");
        Validate.notNull(where, "`where` must not be null");
        Validate.notNull(then, "`then` must not be null");
        return new Rule(binding, where, when, forbid, then);
    }

    public final class StrictRuleBuilder<T extends FactCtx> {
        private StrictRuleBuilder() {}

        public StrictRuleBuilder<T> binding(InputBindingSupplier binding) {
            RuleBuilder.this.binding(binding);
            return this;
        }

        public StrictRuleBuilder<T> where(ActionLocation where) {
            RuleBuilder.this.where(where);
            return this;
        }

        public StrictRuleBuilder<T> when(ResourceLocation... when) {
            RuleBuilder.this.when(when);
            return this;
        }

        @SafeVarargs
        public final StrictRuleBuilder<T> when(Fact<T>... when) {
            for (Fact<T> fact : when) {
                RuleBuilder.this.when(fact.id());
            }
            return this;
        }

        public StrictRuleBuilder<T> forbid(ResourceLocation... when) {
            RuleBuilder.this.forbid(when);
            return this;
        }

        @SafeVarargs
        public final StrictRuleBuilder<T> forbid(Fact<T>... when) {
            for (Fact<T> fact : when) {
                RuleBuilder.this.forbid(fact.id());
            }
            return this;
        }

        public StrictRuleBuilder<T> then(Component then) {
            RuleBuilder.this.then(then);
            return this;
        }

        public Rule build() {
            return RuleBuilder.this.build();
        }

        public RuleBuilder lenient() {
            return RuleBuilder.this;
        }
    }
}
