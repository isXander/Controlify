package dev.isxander.controlify.api.bind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A supplier that retrieves an {@link InputBinding} for a impl.
 */
public interface InputBindingSupplier {
    /**
     * Retrieves the input binding for the impl, throwing an exception if it does not exist.
     *
     * @param controller the impl to fetch the binding from
     * @throws NullPointerException if the binding does not exist
     * @return the input binding
     */
    default InputBinding on(@NonNull ControllerEntity controller) {
        return Objects.requireNonNull(
                onOrNull(controller),
                () -> "Attempted to fetch " + bindId() + " for impl " + controller.uid() + " but it did not exist." +
                        "The binding registry callback may have a filter that did not pass for this impl.");
    }

    /**
     * Retrieves the input binding for the impl, or null if it does not exist.
     *
     * @param controller impl to fetch binding from
     * @return the input binding, or null if it does not exist
     */
    @Nullable InputBinding onOrNull(ControllerEntity controller);

    /**
     * @return id of the binding.
     */
    Identifier bindId();

    /**
     * Returns a text component with the currently bound input glyph
     * for the currently selected impl.
     * <p>
     * Be careful, as this component will be rendered differently
     * depending on the active impl, so make sure to deal with
     * width caching and other issues that may arise.
     * @see InputBinding#inputGlyph() for a deterministic version on a specific impl
     * @return a component representing the input glyph
     */
    Component inputGlyph();

    Codec<InputBindingSupplier> CODEC = Identifier.CODEC.xmap(
            bindId -> ControlifyBindApi.get().createSupplier(bindId),
            InputBindingSupplier::bindId
    );
}
