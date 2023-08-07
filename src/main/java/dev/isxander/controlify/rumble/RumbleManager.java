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
        RumbleEffectInstance effect;
        do {
            effect = effectQueue.peek();

            // if we have no effects, break out of loop and get the null check
            if (effect == null)
                break;

            // if the effect is finished, remove and set null, so we loop again
            if (effect.effect().isFinished()) {
                effectQueue.remove(effect);
                effect = null;
            }
        } while (effect == null);

        if (effect == null) {
            controller.setRumble(0f, 0f, RumbleSource.MASTER);
            return;
        }

        effectQueue.removeIf(e -> e.effect().isFinished());
        effectQueue.forEach(e -> e.effect().tick());

        if (silent) {
            if (!wasSilent) {
                controller.setRumble(0f, 0f, RumbleSource.MASTER);
                wasSilent = true;
            }
        } else {
            RumbleState state = effect.effect().currentState();
            controller.setRumble(state.strong(), state.weak(), effect.source());
        }
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
