package dev.isxander.controlify.utils.animation.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents an object that is animatable.
 */
public sealed interface Animatable permits Animation, AnimationGroup, AnimationSequence {
    /**
     * Start playing the animatable.
     * @return this
     */
    Animatable play();

    /**
     * Ticks the animation
     * @param tickDelta the passed time. 1 = 1 tick
     */
    @ApiStatus.Internal
    void tick(float tickDelta);

    /**
     * Instantly finishes the animatable to the state it would be in if
     * it had time to finish.
     */
    void skipToEnd();

    /**
     * Aborts the animatable and leaves all consumers in the state they were
     * in at the last tick.
     */
    void abort();

    /**
     * @return if the animation has started
     */
    boolean hasStarted();

    /**
     * @return if the animation has completed
     */
    boolean isDone();

    /**
     * @return if the animation is currently in progress
     */
    boolean isPlaying();

    /**
     * Must return an instance that can be played again exactly like the first time.
     */
    Animatable copy();
}
