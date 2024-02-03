package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.bindings.ControllerBindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;
import java.util.function.UnaryOperator;

/**
 * Handles registering new bindings for controllers.
 * <p>
 * Should be called within {@link dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint#onControlifyPreInit(ControlifyApi)}
 */
public interface ControlifyBindingsApi {
    /**
     * Registers a custom binding for all controllers.
     * @param id the identifier for the binding, the namespace should be your modid.
     * @param builder the binding builder function
     * @return the binding supplier to fetch the binding for a specific controller.
     */
    BindingSupplier registerBind(ResourceLocation id, UnaryOperator<ControllerBindingBuilder> builder);

    /**
     * By default, all modded keybindings are registered as controller binds.
     * If you are explicitly depending on Controlify, you should exclude all your
     * keybindings and register them explicitly.
     * @param keyMapping the mappings to exclude
     */
    void excludeVanillaBind(KeyMapping... keyMapping);

    /**
     * Registers a radial icon to be used in the radial menu.
     * The identifier should be passed to {@link ControllerBindingBuilder#radialCandidate(ResourceLocation)}.
     *
     * @param id the identifier for the icon, the namespace should be your modid.
     * @param icon the renderer for the icon.
     */
    void registerRadialIcon(ResourceLocation id, RadialIcon icon);

    static ControlifyBindingsApi get() {
        return ControllerBindings.Api.INSTANCE;
    }
}
