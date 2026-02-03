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
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
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

        Identifier bindId = builder.getIdAndLock();

        this.bindEntries.add(new RegistryEntry(filter, finaliser, builder.getKeyEmulation(), builder.getKeyEmulationToggle(), bindId));

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
    public void registerRadialIcon(Identifier id, RadialIcon icon) {
        checkLocked();

        RadialIcons.registerIcon(id, icon);
    }

    @Override
    public void registerBindContext(BindContext context) {
        BindContext.CONTEXTS.put(context.id(), context);
    }

    private void checkLocked() {
        if (locked)
            throw new IllegalStateException("Registry is locked. Cannot add bind now.");
    }

    /**
     * Provides all bindings for a given controller.
     * @param controller The controller to provide bindings for. Can be null to get a list of all bindings.
     * @return A list of input bindings.
     */
    public List<InputBinding> provideBindsForController(@Nullable ControllerEntity controller) {
        List<InputBinding> bindings = new ArrayList<>();

        for (RegistryEntry entry : bindEntries) {
            if (controller != null && !entry.filter().test(controller))
                continue;

            InputBindingImpl binding = entry.builder().apply(controller);

            if (controller != null && entry.emulation() != null) {
                BooleanSupplier emulationToggle = null;
                if (entry.emulationToggle() != null) {
                    emulationToggle = () -> entry.emulationToggle().apply(controller);
                }

                binding.addDigitalOutput(
                        InputBinding.KEY_EMULATION,
                        new KeyMappingEmulationOutput(controller, binding, entry.emulation(), emulationToggle)
                );
            }

            bindings.add(binding);
        }

        return bindings;
    }

    @Override
    public InputBindingSupplier createSupplier(Identifier bindingId) {
        return new InputBindingSupplierImpl(bindingId);
    }

    public void lock() {
        this.locked = true;
    }

    @Override
    public Stream<Identifier> getAllBindIds() {
        return this.bindEntries.stream().map(RegistryEntry::id);
    }

    private record RegistryEntry(
            Predicate<ControllerEntity> filter,
            Function<@Nullable ControllerEntity, InputBindingImpl> builder,
            KeyMapping emulation,
            Function<ControllerEntity, Boolean> emulationToggle,
            Identifier id
    ) {}
}
