package dev.isxander.controlify.bindings;

import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.bindings.output.KeyMappingEmulationOutput;
import dev.isxander.controlify.controller.ControllerEntity;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ControlifyBindApiImpl implements ControlifyBindApi {
    public static final ControlifyBindApiImpl INSTANCE = new ControlifyBindApiImpl();

    private final List<RegistryEntry> bindEntries = new ArrayList<>();
    private final Map<KeyMapping, List<InputBindingSupplier>> keyMappingCorrelations = new HashMap<>();

    private boolean locked;

    @Override
    public InputBindingSupplier registerBinding(RegistryCallback callback) {
        return registerBinding(callback, c -> true);
    }

    @Override
    public InputBindingSupplier registerBinding(RegistryCallback callback, Predicate<ControllerEntity> filter) {
        checkLocked();

        var builder = new InputBindingBuilderImpl();

        callback.apply(builder);

        Function<ControllerEntity, InputBindingImpl> finaliser = builder::build;

        ResourceLocation bindId = builder.getIdAndLock();

        this.bindEntries.add(new RegistryEntry(filter, finaliser, builder.getKeyEmulation(), bindId));

        for (KeyMapping key : builder.getKeyCorrelations()) {
            keyMappingCorrelations.computeIfAbsent(key, k -> new ArrayList<>()).add(createSupplier(bindId));
        }

        return createSupplier(bindId);
    }

    @Override
    public List<InputBindingSupplier> getKeyCorrelation(KeyMapping key) {
        return Optional.ofNullable(this.keyMappingCorrelations.get(key)).orElse(List.of());
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

            InputBindingImpl binding = entry.builder().apply(controller);

            if (entry.emulation() != null) {
                binding.addDigitalOutput(
                        InputBinding.KEY_EMULATION,
                        new KeyMappingEmulationOutput(binding, entry.emulation())
                );
            }

            bindings.add(binding);
        }

        return bindings;
    }

    private InputBindingSupplier createSupplier(ResourceLocation bindingId) {
        return new InputBindingSupplier() {
            @Override
            public InputBinding on(ControllerEntity controller) {
                return controller.input().map(input -> input.getBinding(bindingId)).orElse(null);
            }

            @Override
            public ResourceLocation bindId() {
                return bindingId;
            }
        };
    }

    public void lock() {
        this.locked = true;
    }

    @Override
    public Stream<ResourceLocation> getAllBindIds() {
        return this.bindEntries.stream().map(RegistryEntry::id);
    }

    private record RegistryEntry(
            Predicate<ControllerEntity> filter,
            Function<ControllerEntity, InputBindingImpl> builder,
            KeyMapping emulation,
            ResourceLocation id
    ) {}
}
