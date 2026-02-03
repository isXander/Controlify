package dev.isxander.controlify.platform.client.resource;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Collection;
import java.util.Collections;

public interface ControlifyReloadListener
        extends PreparableReloadListener
        //? if fabric
        ,net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
{
    Identifier getReloadId();

    default Collection<Identifier> getDependencies() {
        return Collections.emptyList();
    }

    //? if fabric {
    @Override
    default Identifier getFabricId() {
        return this.getReloadId();
    }

    @Override
    default Collection<Identifier> getFabricDependencies() {
        return this.getDependencies();
    }
    //?}
}
