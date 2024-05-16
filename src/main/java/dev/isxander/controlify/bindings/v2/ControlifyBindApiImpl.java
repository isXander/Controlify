package dev.isxander.controlify.bindings.v2;

import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.bindings.RadialIcons;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

public class ControlifyBindApiImpl implements ControlifyBindApi {
    public static final ControlifyBindApiImpl INSTANCE = new ControlifyBindApiImpl();

    private final List<RegistryEntry> bindEntries = new ArrayList<>();
    private final Map<KeyMapping, InputBindingSupplier> keyMappingCorrelations = new HashMap<>();

    private boolean locked;

    @Override
    public InputBindingSupplier registerBinding(RegistryCallback callback) {
        return registerBinding(callback, c -> true);
    }

    @Override
    public InputBindingSupplier registerBinding(RegistryCallback callback, Predicate<ControllerEntity> filter) {
        checkLocked();

        var builder = new InputBindingBuilderImpl();
        var context = new BindRegistrationContextImpl();

        callback.buildBinding(builder, context);

        Function<ControllerEntity, InputBinding> finaliser = builder::build;

        this.bindEntries.add(new RegistryEntry(filter, finaliser));

        ResourceLocation bindId = builder.getIdAndLock();

        for (KeyMapping key : context.correlations) {
            boolean correlationExists = keyMappingCorrelations.put(key, createSupplier(bindId)) != null;
            if (correlationExists) {
                CUtil.LOGGER.warn("Duplicate correlation for input binding. Overwritten.");
            }
        }

        return createSupplier(bindId);
    }

    @Override
    public Optional<InputBindingSupplier> getKeyCorrelation(KeyMapping key) {
        return Optional.ofNullable(this.keyMappingCorrelations.get(key));
    }

    @Override
    public void registerRadialIcon(ResourceLocation id, RadialIcon icon) {
        checkLocked();

        RadialIcons.registerIcon(id, icon);
    }

    @Override
    public void registerBindContext(BindContext context) {
        Registry.register(BindContext.REGISTRY, context.id(), context);
    }

    private void checkLocked() {
        if (locked)
            throw new IllegalStateException("Registry is locked. Cannot add bind now.");
    }

    public List<InputBinding> provideBindsForController(ControllerEntity controller) {
        List<InputBinding> bindings = new ArrayList<>();

        for (RegistryEntry entry : bindEntries) {
            if (!entry.filter().test(controller))
                continue;

            InputBinding binding = entry.builder().apply(controller);
            bindings.add(binding);
        }

        return bindings;
    }

    private InputBindingSupplier createSupplier(ResourceLocation bindingId) {
        return controller -> controller.input().map(input -> input.getBinding(bindingId)).orElse(null);
    }

    public void lock() {
        this.locked = true;
    }

    private record RegistryEntry(Predicate<ControllerEntity> filter, Function<ControllerEntity, InputBinding> builder) {
    }

    private static class BindRegistrationContextImpl implements BindRegistrationContext {
        public List<KeyMapping> correlations = new ArrayList<>();
        public List<MappingEmulation> emulations = new ArrayList<>();

        @Override
        public void createKeyMappingCorrelation(KeyMapping keyMapping) {
            this.correlations.add(keyMapping);
        }

        @Override
        public void emulateKeyMapping(KeyMapping keyMapping, BooleanSupplier toggleable) {
            this.emulations.add(new MappingEmulation(keyMapping, toggleable));
        }

        public record MappingEmulation(KeyMapping keyMapping, BooleanSupplier toggleable) {
        }
    }
}
