package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.ControlifyBindApiImpl;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Predicate;

public interface ControlifyBindApi {
    static ControlifyBindApi get() {
        return ControlifyBindApiImpl.INSTANCE;
    }

    InputBindingSupplier registerBinding(RegistryCallback callback);

    InputBindingSupplier registerBinding(RegistryCallback callback, Predicate<ControllerEntity> filter);

    List<InputBindingSupplier> getKeyCorrelation(KeyMapping key);

    void registerRadialIcon(ResourceLocation id, RadialIcon icon);

    void registerBindContext(BindContext context);

    @FunctionalInterface
    interface RegistryCallback {
        InputBindingBuilder buildBinding(InputBindingBuilder builder, BindRegistrationContext ctx);
    }

    interface BindRegistrationContext {
        void createKeyMappingCorrelation(KeyMapping keyMapping);

        void emulateKeyMapping(KeyMapping keyMapping);
    }
}
