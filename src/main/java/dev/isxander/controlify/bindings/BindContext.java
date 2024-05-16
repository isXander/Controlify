package dev.isxander.controlify.bindings;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class BindContext {
    private final ResourceLocation context;
    private final Set<BindContext> parents;
    private final Set<ResourceLocation> flattened;

    public BindContext(ResourceLocation context, Set<BindContext> parents) {
        this.context = context;
        this.parents = parents;

        Set<ResourceLocation> flattened = new HashSet<>();
        flattened.add(context());
        parents().forEach(p -> flattened.addAll(p.flattened()));
        this.flattened = ImmutableSet.copyOf(flattened);
    }

    public ResourceLocation context() {
        return context;
    }

    public Set<BindContext> parents() {
        return parents;
    }

    public Set<ResourceLocation> flattened() {
        return flattened;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BindContext) obj;
        return Objects.equals(this.context, that.context) &&
                Objects.equals(this.parents, that.parents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, parents);
    }

    @Override
    public String toString() {
        return "BindContext[" +
                "id=" + context + ", " +
                "parents=" + parents + ']';
    }

    public static Set<ResourceLocation> flatten(Set<BindContext> contexts) {
        return contexts.stream()
                .flatMap(ctx -> ctx.flattened.stream())
                .collect(Collectors.toSet());
    }
}
