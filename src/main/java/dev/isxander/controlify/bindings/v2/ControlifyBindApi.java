package dev.isxander.controlify.bindings.v2;

import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public interface ControlifyBindApi {
    static ControlifyBindApi get() {
        return ControlifyBindApiImpl.INSTANCE;
    }

    InputBindingSupplier registerBinding(RegistryCallback callback);

    InputBindingSupplier registerBinding(RegistryCallback callback, Predicate<ControllerEntity> filter);

    Optional<InputBindingSupplier> getKeyCorrelation(KeyMapping key);

    void registerRadialIcon(ResourceLocation id, RadialIcon icon);

    void registerBindContext(BindContext context);

    @FunctionalInterface
    interface RegistryCallback {
        InputBindingBuilder buildBinding(InputBindingBuilder builder, BindRegistrationContext ctx);
    }

    interface BindRegistrationContext {
        void createKeyMappingCorrelation(KeyMapping keyMapping);

        void emulateKeyMapping(KeyMapping keyMapping, BooleanSupplier toggleable);
    }
}
