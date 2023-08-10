package dev.isxander.controlify.rumble;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class RumbleManager {
    private final RumbleCapable controller;
    private final Queue<RumbleEffectInstance> effectQueue;

    private boolean silent, wasSilent;

    public RumbleManager(RumbleCapable controller) {
        this.controller = controller;
        this.effectQueue = new PriorityQueue<>(Comparator.comparing(RumbleEffectInstance::effect));
    }

    @Deprecated
    public void play(RumbleEffect effect) {
        play(RumbleSource.MASTER, effect);
    }

    public void play(RumbleSource source, RumbleEffect effect) {
        if (!controller.supportsRumble())
            return;

        effectQueue.add(new RumbleEffectInstance(source, effect));
    }

    public void tick() {
        effectQueue.removeIf(e -> e.effect().isFinished());
        effectQueue.forEach(e -> e.effect().tick());

        if (effectQueue.isEmpty()) {
            clearRumble();
            return;
        }

        float strong = 0f, weak = 0f;
        for (RumbleEffectInstance effect : effectQueue) {
            RumbleState effectState = controller.applyRumbleSourceStrength(effect.effect().currentState(), effect.source());
            strong = Math.max(strong, effectState.strong());
            weak = Math.max(weak, effectState.weak());
        }
        RumbleState state = new RumbleState(strong, weak);

        if (state.isZero()) {
            clearRumble();
            return;
        }

        if (silent) {
            clearRumble();
        } else {
            controller.setRumble(state.strong(), state.weak());
            wasSilent = false;
        }
    }

    private void clearRumble() {
        if (wasSilent)
            return;

        controller.setRumble(0f, 0f);
        wasSilent = true;
    }

    public void clearEffects() {
        effectQueue.clear();
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isPlaying() {
        return !effectQueue.isEmpty();
    }

    private record RumbleEffectInstance(RumbleSource source, RumbleEffect effect) implements Comparable<RumbleEffectInstance> {
        @Override
        public int compareTo(@NotNull RumbleManager.RumbleEffectInstance o) {
            return effect.compareTo(o.effect);
        }
    }
}
