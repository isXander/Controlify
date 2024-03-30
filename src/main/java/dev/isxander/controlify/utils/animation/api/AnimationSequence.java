package dev.isxander.controlify.utils.animation.api;

import dev.isxander.controlify.utils.animation.impl.AnimationSequenceImpl;

/**
 * A sequence of {@link Animatable}s where each animatable is played one
 * after the other, and is only finished when the last child finishes.
 */
public non-sealed interface AnimationSequence extends Animatable {
    /**
     * Creates a new animation sequence with no animatables.
     * You can add them with {@link AnimationSequence#push(Animatable...)}.
     * @return a new instance of {@link AnimationSequence}
     */
    static AnimationSequence of() {
        return new AnimationSequenceImpl();
    }

    /**
     * Creates a new animation sequence with a pre-determined sequence.
     * The first element in the array is played first, the last is played last.
     * @param animatables an array of animatables
     * @return a new instance of {@link AnimationSequence}
     */
    static AnimationSequence of(Animatable... animatables) {
        return of().push(animatables);
    }

    /**
     * Push an animatable into the sequence.
     * This can happen even when the animation has started playing,
     * so long as it hasn't finished.
     *
     * @param animatables the animatables to add to the end of the sequence
     * @return this
     */
    AnimationSequence push(Animatable... animatables);

    @Override
    AnimationSequence play();

    @Override
    AnimationSequence copy();
}
