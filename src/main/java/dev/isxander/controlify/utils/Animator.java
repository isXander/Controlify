package dev.isxander.controlify.utils;

import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class Animator {
    private final List<AnimationConsumer> animations;
    private final UnaryOperator<Float> easingFunction;
    private final int durationTicks;
    private float time;

    public Animator(int durationTicks, UnaryOperator<Float> easingFunction) {
        this.animations = new ArrayList<>();
        this.easingFunction = easingFunction;
        this.durationTicks = durationTicks;
    }

    public void addConsumer(Consumer<Float> consumer, float start, float end) {
        animations.add(new AnimationConsumer(consumer, start, end));
    }

    public void addConsumer(Consumer<Integer> consumer, int start, int end) {
        animations.add(new AnimationConsumer(aFloat -> consumer.accept(aFloat.intValue()), start, end));
    }

    public void tick(float deltaTime) {
        time += deltaTime;
        if (time > durationTicks) {
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

    public boolean isDone() {
        return time >= durationTicks;
    }

    private record AnimationConsumer(Consumer<Float> consumer, float start, float end) {
    }
}
