package dev.isxander.controlify.controller.id;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ControllerTypeManager {

    public static final Registry<ControllerType> REGISTRY = new MappedRegistry<>(
            ResourceKey.createRegistryKey(new ResourceLocation("controlify", "controller_type")),
            Lifecycle.stable()
    );


}
