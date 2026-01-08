package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.ControlifyBindApiImpl;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * The API for registering and managing bindings.
 */
public interface ControlifyBindApi {
    static ControlifyBindApi get() {
        return ControlifyBindApiImpl.INSTANCE;
    }

    /**
     * Registers a new binding that will be created on all controllers.
     *
     * @param callback the callback that populates the binding builder. will only be called once
     * @return the supplier for the binding
     */
    InputBindingSupplier registerBinding(RegistryCallback callback);

    /**
     * Registers a new binding that will be created on only the controllers that pass the filter.
     *
     * @param callback the callback that populates the binding builder. will only be called once
     * @param filter the filter that determines if the binding should be created for the impl
     * @return the supplier for the binding
     */
    InputBindingSupplier registerBinding(RegistryCallback callback, Predicate<ControllerEntity> filter);

    /**
     * Creates a supplier for an input binding with the given bind ID.
     * Useful when fetching bindings by ID, rather than by java reference, such is needed
     * in cases of data-driven systems.
     * {@link InputBindingSupplier#on(ControllerEntity)} may throw an exception if the binding does not exist,
     * when it is attempted to be resolved.
     * @param bindId the ID of the binding to create a supplier for
     * @return the supplier for the binding
     */
    InputBindingSupplier createSupplier(Identifier bindId);

    /**
     * Get all the impl bindings for a vanilla key mapping.
     * This is populated using {@link InputBindingBuilder#addKeyCorrelation(KeyMapping)}
     * or {@link InputBindingBuilder#keyEmulation(KeyMapping)}.
     *
     * @param key the key mapping to get the correlations for
     * @return the list of the suppliers for the binding
     */
    List<InputBindingSupplier> getKeyCorrelation(KeyMapping key);

    /**
     * Registers a new radial icon that can be used in the radial menu.
     *
     * @param id the id of the icon
     * @param icon the icon object
     */
    void registerRadialIcon(Identifier id, RadialIcon icon);

    /**
     * Registers a new bind context that can be used to determine when a binding is active.
     *
     * @param context the bind context
     */
    void registerBindContext(BindContext context);

    /**
     * Gets all registered bind contexts.
     *
     * @return the stream of bind contexts
     */
    Stream<Identifier> getAllBindIds();

    @FunctionalInterface
    interface RegistryCallback extends UnaryOperator<InputBindingBuilder> {
    }
}
