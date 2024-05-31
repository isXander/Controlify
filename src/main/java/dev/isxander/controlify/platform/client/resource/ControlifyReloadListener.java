package dev.isxander.controlify.platform.client.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Collection;
import java.util.Collections;

public interface ControlifyReloadListener
        extends PreparableReloadListener
        //? if fabric
        /*,net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener*/
{
    ResourceLocation getReloadId();

    default Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    //? if fabric {
    /*@Override
    default ResourceLocation getFabricId() {
        return this.getReloadId();
    }

    @Override
    default Collection<ResourceLocation> getFabricDependencies() {
        return this.getDependencies();
    }
    *///?}
}
