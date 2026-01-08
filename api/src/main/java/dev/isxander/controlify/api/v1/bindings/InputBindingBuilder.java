package dev.isxander.controlify.api.v1.bindings;

import dev.isxander.controlify.api.CID;
import org.jspecify.annotations.NonNull;

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
    InputBindingBuilder id(@NonNull CID rl);

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
     * @param namespace namespace of {@link CID}
     * @param path path of {@link CID}
     * @return this builder
     */
    default InputBindingBuilder id(@NonNull String namespace, @NonNull String path) {
        return this.id(new CID(namespace, path));
    }

    /**
     * Sets the category for the binding. <strong>Required field.</strong>
     * This is used to group the binding in the option menu.
     * It's recommended that you use a single category for all of your mod's bindings.
     *
     * @param category the category
     * @return this builder
     */
    InputBindingBuilder category(@NonNull InputBindingCategory category);

    /**
     * Registers this binding as a radial menu candidate with the given icon.
     * @param icon the radial icon
     * @return this builder
     */
    InputBindingBuilder radialCandidate(@NonNull RadialIcon icon);

}
