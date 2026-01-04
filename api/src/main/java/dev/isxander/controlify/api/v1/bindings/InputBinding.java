package dev.isxander.controlify.api.v1.bindings;

import dev.isxander.controlify.api.CIdentifier;
import dev.isxander.controlify.api.MinecraftComponent;

public interface InputBinding {
    CIdentifier id();

    @MinecraftComponent Object glyphIcon();

    /**
     * Gets the current analogue value of this binding.
     * In the range [0.0, 1.0].
     * <p>
     * When the underlying binding is digital,
     * this returns 1.0 if the digital value is true, otherwise 0.
     */
    float analogueNow();

    /**
     * Gets the previous tick analogue value of this binding.
     * In the range [0.0, 1.0].
     * <p>
     * When the underlying binding is digital,
     * this returns 1.0 if the digital value is true, otherwise 0.
     */
    float analoguePrev();

    /**
     * Gets the current digital value of this binding.
     * <p>
     * When the underlying binding is analogue,
     * this returns true if the analogue value is greater than the configured threshold.
     */
    boolean digitalNow();

    /**
     * Gets the previous tick digital value of this binding.
     * <p>
     * When the underlying binding is analogue,
     * this returns true if the analogue value is greater than the configured threshold.
     */
    boolean digitalPrev();

    /**
     * Whether the binding was just pressed this tick.
     * <p>
     * Equivalent to calling
     * <pre><code>
     *     binding.digitalNow() && !binding.digitalPrev()
     * </code></pre>
     */
    boolean justPressed();

    /**
     * Whether the binding was just released this tick.
     * <p>
     * Equivalent to calling
     * <pre><code>
     *     !binding.digitalNow() && binding.digitalPrev()
     * </code></pre>
     */
    boolean justReleased();

    /**
     * Whether the binding was just tapped (pressed and released).
     */
    boolean justTapped();

    /**
     * Similar to {@link #justTapped()}, but with infinite max hold time,
     * and is canceled by GUI navigation.
     * <p>
     * In practice, this means if a user held down the binding when hovering a button,
     * then whilst still holding it, navigated to another button, and then released, this would not activate.
     */
    boolean guiPressed();
}
