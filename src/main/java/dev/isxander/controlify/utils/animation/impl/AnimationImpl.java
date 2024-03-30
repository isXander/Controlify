package dev.isxander.controlify.utils.animation.impl;

import dev.isxander.controlify.utils.animation.api.Animation;
import dev.isxander.controlify.utils.animation.api.EasingFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.function.Consumer;

public class AnimationImpl implements Animation {
    private final List<AnimationConsumer> consumers;
    private final List<AnimationDeltaConsumer> deltaConsumers;

    private EasingFunction easing = EasingFunction.LINEAR;

    private float time;
    private int duration;

    private boolean done;

    public AnimationImpl() {
        this.consumers = new ObjectArrayList<>();
        this.deltaConsumers = new ObjectArrayList<>();
    }

    public AnimationImpl(AnimationImpl other) {
        this.consumers = new ObjectArrayList<>(other.consumers);
        this.deltaConsumers = new ObjectArrayList<>(other.deltaConsumers);
        this.easing = other.easing;
        this.time = other.time;
        this.duration = other.duration;
        this.done = other.done;
    }

    @Override
    public Animation consumerI(Consumer<Integer> consumer, double start, double end) {
        consumers.add(new AnimationConsumer(d -> consumer.accept((int) (double) d), start, end));
        return this;
    }

    @Override
    public Animation consumerF(Consumer<Float> consumer, double start, double end) {
        consumers.add(new AnimationConsumer(d -> consumer.accept((float) (double) d), start, end));
        return this;
    }

    @Override
    public Animation consumerD(Consumer<Double> consumer, double start, double end) {
        consumers.add(new AnimationConsumer(consumer, start, end));
        return this;
    }

    @Override
    public Animation deltaConsumerI(Consumer<Integer> consumer, double start, double end) {
        deltaConsumers.add(new AnimationDeltaConsumer(d -> consumer.accept((int) (double) d), start, end));
        return this;
    }

    @Override
    public Animation deltaConsumerF(Consumer<Float> consumer, double start, double end) {
        deltaConsumers.add(new AnimationDeltaConsumer(d -> consumer.accept((float) (double) d), start, end));
        return this;
    }

    @Override
    public Animation deltaConsumerD(Consumer<Double> consumer, double start, double end) {
        deltaConsumers.add(new AnimationDeltaConsumer(consumer, start, end));
        return this;
    }

    @Override
    public Animation duration(int ticks) {
        this.duration = ticks;
        return this;
    }

    @Override
    public Animation easing(EasingFunction easing) {
        this.easing = easing;
        return this;
    }

    @Override
    public Animation copy() {
        return new AnimationImpl(this);
    }

    @Override
    public void tick(float tickDelta) {
        if (duration <= 0 || time >= duration) {
            done = true;
        }

        if (done) return;

        time += tickDelta;

        updateConsumers();
    }

    private void updateConsumers() {
        float progress = easing.ease(time / duration);
        consumers.forEach(consumer -> consumer.tick(progress));
        deltaConsumers.forEach(consumer -> consumer.tick(progress));
    }

    @Override
    public void skipToEnd() {
        time = duration;
        updateConsumers();
        done = true;
    }

    @Override
    public void abort() {
        done = true;
    }

    @Override
    public Animation play() {
        Animator.INSTANCE.add(this);
        return this;
    }

    @Override
    public boolean hasStarted() {
        return time > 0;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean isPlaying() {
        return hasStarted() && !isDone();
    }

    private record AnimationConsumer(Consumer<Double> consumer, double start, double end) {
        public void tick(float tickDelta) {
            consumer.accept(start + (end - start) * tickDelta);
        }
    }

    private static class AnimationDeltaConsumer {
        private final Consumer<Double> consumer;
        private final double start;
        private final double end;

        private double lastValue;

        public AnimationDeltaConsumer(Consumer<Double> consumer, double start, double end) {
            this.consumer = consumer;
            this.start = start;
            this.end = end;
        }

        public void tick(float tickDelta) {
            double value = start + (end - start) * tickDelta;
            consumer.accept(value - lastValue);
            lastValue = value;
        }
    }
}
