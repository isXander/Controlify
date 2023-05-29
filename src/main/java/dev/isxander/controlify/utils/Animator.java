package dev.isxander.controlify.utils;

import net.minecraft.util.Mth;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class Animator {
    public static final Animator INSTANCE = new Animator();

    private final List<AnimationInstance> animations;

    private Animator() {
        this.animations = new ArrayList<>();
    }

    public void progress(float deltaTime) {
        animations.forEach(animation -> animation.tick(deltaTime));
        animations.removeIf(AnimationInstance::isDone);
    }

    public AnimationInstance play(AnimationInstance animation) {
        animations.add(animation);
        return animation;
    }

    public static class AnimationInstance {
        private final List<AnimationConsumer> animations;
        private final UnaryOperator<Float> easingFunction;
        private final int durationTicks;
        private float time;
        private boolean done;
        private final List<Runnable> callbacks = new ArrayList<>();

        public AnimationInstance(int durationTicks, UnaryOperator<Float> easingFunction) {
            this.animations = new ArrayList<>();
            this.easingFunction = easingFunction;
            this.durationTicks = durationTicks;
        }

        public AnimationInstance addConsumer(Consumer<Float> consumer, float start, float end) {
            animations.add(new AnimationConsumer(consumer, start, end));
            return this;
        }

        public AnimationInstance addConsumer(Consumer<Integer> consumer, int start, int end) {
            animations.add(new AnimationConsumer(aFloat -> consumer.accept(aFloat.intValue()), start, end));
            return this;
        }

        public AnimationInstance onComplete(Runnable callback) {
            callbacks.add(callback);
            return this;
        }

        private void tick(float deltaTime) {
            time += deltaTime;
            if (time > durationTicks) {
                if (!done) {
                    callbacks.removeIf(callback -> {
                        callback.run();
                        return true;
                    });
                }
                done = true;
                time = durationTicks;
            }

            updateConsumers();
        }

        private void updateConsumers() {
            animations.forEach(consumer -> {
                float progress = easingFunction.apply(time / durationTicks);
                float value = Mth.lerp(progress, consumer.start, consumer.end);
                consumer.consumer.accept(value);
            });
        }

        public void finish() {
            time = durationTicks;
            updateConsumers();
        }

        public boolean isDone() {
            return done;
        }

        private record AnimationConsumer(Consumer<Float> consumer, float start, float end) {
        }
    }
}
