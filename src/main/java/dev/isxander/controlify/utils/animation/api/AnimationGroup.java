package dev.isxander.controlify.utils.animation.api;

import dev.isxander.controlify.utils.animation.impl.AnimationGroupImpl;

/**
 * A group of {@link Animatable}s where all are played concurrently
 * and the group is finished only when all children are finished.
 */
public non-sealed interface AnimationGroup extends Animatable {
    /**
     * Creates a new animation group with no animatables.
     * You can add animatables with {@link AnimationGroup#add(Animatable...)}
     * @return a new instance of {@link AnimationGroup}
     */
    static AnimationGroup of() {
        return new AnimationGroupImpl();
    }

    /**
     * Creates a new animation group with pre-determined animatables.
     * You can still add more animtables with {@link AnimationGroup#add(Animatable...)}
     * @param animation the array of animatables to add to the group
     * @return a new instance of {@link AnimationGroup}
     */
    static AnimationGroup of(Animatable... animation) {
        return of().add(animation);
    }

    /**
     * Adds more animatable to the group.
     * You cannot call this method after the group has begun to play.
     * @param animation the animatables to add
     * @return this
     */
    AnimationGroup add(Animatable... animation);

    @Override
    AnimationGroup play();

    @Override
    AnimationGroup copy();
}
