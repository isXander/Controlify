package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A builder for creating an {@link InputBinding}.
 * This is used to populate the fields of the binding.
 * The builder is provided through a callback in
 * {@link ControlifyBindApi#registerBinding(ControlifyBindApi.RegistryCallback)}
 * and should not be created or built manually.
 */
public interface InputBindingBuilder {
    /**
     * Sets the unique identifier for the binding.
     * This is a required field. The namespace should be your mod's ID.
     * <p>
     * The id is used to automatically generate the translation keys for the name and description.
     * <ul>
     *     <li>Name: <code>controlify.binding.(namespace).(path)</code></li>
     *     <li>Desc: <code>controlify.binding.(namespace).(path).desc</code>, optional</li>
     * </ul>
     * This is the ID referred to in {@link InputBindingSupplier#bindId()}.
     *
     * @param rl the binding's id
     * @return this builder
     */
    InputBindingBuilder id(@NotNull ResourceLocation rl);

    /**
     * Sets the unique identifier for the binding.
     * This is a required field. The namespace should be your mod's ID.
     * <p>
     * The id is used to automatically generate the translation keys for the name and description.
     * <ul>
     *     <li>Name: <code>controlify.binding.(namespace).(path)</code></li>
     *     <li>Desc: <code>controlify.binding.(namespace).(path).desc</code>, optional</li>
     * </ul>
     * This is the ID referred to in {@link InputBindingSupplier#bindId()}.
     *
     * @param namespace namespace of {@link ResourceLocation}
     * @param path path of {@link ResourceLocation}
     * @return this builder
     */
    InputBindingBuilder id(@NotNull String namespace, @NotNull String path);

    /**
     * Sets a custom name component for the binding. Optional field.
     * If not set, the name will be automatically generated from {@link #id(ResourceLocation)},
     * <code>controlify.binding.(namespace).(path)</code>
     * <p>
     * Useful for re-using language keys from regular key mappings.
     *
     * @param text the name component
     * @return this builder
     */
    InputBindingBuilder name(@NotNull Component text);

    /**
     * Sets a custom description component for the binding. Optional field.
     * If not set, the description will be automatically generated from {@link #id(ResourceLocation)},
     * <code>controlify.binding.(namespace).(path).desc</code>
     * The description will be empty if the translation key does not exist.
     * <p>
     * Useful for re-using language keys from regular key mappings.
     *
     * @param text the name component
     * @return this builder
     */
    InputBindingBuilder description(@NotNull Component text);

    /**
     * Sets the category for the binding. <strong>Required field.</strong>
     * This is used to group the binding in the option menu.
     * It's recommended that you use a single category for all of your mod's bindings.
     *
     * @param text the category component
     * @return this builder
     */
    InputBindingBuilder category(@NotNull Component text);

    /**
     * Sets the default input for the binding.
     * <strong>It is not recommended to use this method.</strong> Default inputs should be data-driven.
     * If specified, the default will be bottom-most layered with data-driven defaults.
     * To set the default input from data, specify it in:
     * <pre>assets/controlify/controllers/default_bind/default.json</pre>
     * <pre><code>
     *     {
     *         "defaults": {
     *             "your_binding_id": {
     *                 "button": "controlify:button/south"
     *             }
     *         }
     *     }
     * </code></pre>
     * @param input the default input
     * @return this builder
     */
    InputBindingBuilder defaultInput(@Nullable Input input);

    /**
     * Sets the contexts that the binding is allowed to be active in.
     * If the binding's predicate {@link BindContext#isApplicable()} returns false, all outputs will be disabled.
     *
     * @param contexts all applicable contexts
     * @return this builder
     */
    InputBindingBuilder allowedContexts(@NotNull BindContext @Nullable... contexts);

    /**
     * Specifies an icon that can be used in the radial menu.
     * Also allows for this binding to be selected in the radial menu editor screen.
     *
     * @param icon the ID of the icon
     * @return this builder
     */
    InputBindingBuilder radialCandidate(@Nullable ResourceLocation icon);

    /**
     * Adds a correlation between a vanilla key mapping and this binding.
     * A correlation means that an auto-generated binding for this vanilla key mapping will not be created.
     *
     * @param keyMapping the key mapping to correlate with
     * @return this builder
     */
    InputBindingBuilder addKeyCorrelation(@NotNull KeyMapping keyMapping);

    /**
     * Makes the binding emulate a vanilla key mapping.
     * If the bound input is pressed, so will the key mapping.
     *
     * @param keyMapping the key mapping to emulate
     * @return this builder
     */
    InputBindingBuilder keyEmulation(@NotNull KeyMapping keyMapping);

    InputBindingBuilder keyEmulation(@NotNull KeyMapping keyMapping, @Nullable Function<ControllerEntity, Boolean> toggleCondition);
}
