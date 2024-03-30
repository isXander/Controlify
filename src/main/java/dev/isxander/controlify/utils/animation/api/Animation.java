package dev.isxander.controlify.utils.animation.api;


import dev.isxander.controlify.utils.animation.impl.AnimationImpl;

import java.util.function.Consumer;

/**
 * Represents an actual animation that has an effect on something.
 * <br>
 * Animations have a list of consumers, so you can apply multiple effects
 * with the same animation. You consume the animation with any of the consumer
 * methods which follow an interpolation along the {@link Animation#easing(EasingFunction)} function.
 *
 * @see Animation#of(int) method to create an animation
 */
public non-sealed interface Animation extends Animatable {
    /**
     * Creates a new instance of an animation.
     * All methods that configure something about the animation are locked
     * after {@link Animation#play()} is called.
     * <br>
     * {@link Animation#duration(int)} must be called for this to be a playable animation.
     *
     * @return a new animation instance
     */
    static Animation of() {
        return new AnimationImpl();
    }

    /**
     * Creates a new instance of an animation.
     * All methods that configure something about the animation are locked
     * after {@link Animation#play()} is called.
     *
     * @param durationTicks the amount of ticks the animation takes. shorthand of {@link Animation#duration(int)}
     * @return a new animation instance
     */
    static Animation of(int durationTicks) {
        return of().duration(durationTicks);
    }

    /**
     * Adds an integer consumer to the animation.
     *
     * @param consumer the current value of this consumer
     * @param start the starting point for this animation consumer
     * @param end the ending point for this animation consumer
     * @return this
     */
    Animation consumerI(Consumer<Integer> consumer, double start, double end);

    /**
     * Adds a float consumer to the animation.
     *
     * @param consumer the current value of this consumer
     * @param start the starting point for this animation consumer
     * @param end the ending point for this animation consumer
     * @return this
     */
    Animation consumerF(Consumer<Float> consumer, double start, double end);

    /**
     * Adds a double consumer to the animation.
     *
     * @param consumer the setter
     * @param start the starting point for this animation consumer
     * @param end the ending point for this animation consumer
     * @return this
     */
    Animation consumerD(Consumer<Double> consumer, double start, double end);

    /**
     * Adds an integer delta consumer to the animation.
     * A delta consumer is like a regular consumer but only consumes the delta since the
     * previous tick of the animation.
     *
     * @param consumer the setter
     * @param start the starting point for this consumer
     * @param end the ending point for this consumer
     * @return this
     */
    Animation deltaConsumerI(Consumer<Integer> consumer, double start, double end);

    /**
     * Adds a float delta consumer to the animation.
     * A delta consumer is like a regular consumer but only consumes the delta since the
     * previous tick of the animation.
     *
     * @param consumer the setter
     * @param start the starting point for this consumer
     * @param end the ending point for this consumer
     * @return this
     */
    Animation deltaConsumerF(Consumer<Float> consumer, double start, double end);

    /**
     * Adds a double delta consumer to the animation.
     * A delta consumer is like a regular consumer but only consumes the delta since the
     * previous tick of the animation.
     *
     * @param consumer the setter
     * @param start the starting point for this consumer
     * @param end the ending point for this consumer
     * @return this
     */
    Animation deltaConsumerD(Consumer<Double> consumer, double start, double end);

    /**
     * Specifies the duration of the animation, in ticks.
     * 20 ticks is equal to 1 second.
     * @param ticks length of animation, in ticks
     * @return this
     */
    Animation duration(int ticks);

    /**
     * Specify the easing function for all animation consumers.
     * @see EasingFunction
     * @param easing the easing function to use
     * @return this
     */
    Animation easing(EasingFunction easing);

    @Override
    Animation copy();

    @Override
    Animation play();
}
