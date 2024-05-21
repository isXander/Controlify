package dev.isxander.controlify.platform.client.resource;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;

public interface ControlifyReloadListener extends IdentifiableResourceReloadListener {
    ResourceLocation getReloadId();

    default Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    default ResourceLocation getFabricId() {
        return this.getReloadId();
    }

    @Override
    default Collection<ResourceLocation> getFabricDependencies() {
        return this.getDependencies();
    }
}
