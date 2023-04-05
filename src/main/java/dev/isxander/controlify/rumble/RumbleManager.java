package dev.isxander.controlify.rumble;

public class RumbleManager {
    private final RumbleCapable controller;
    private RumbleEffectInstance playingEffect;

    public RumbleManager(RumbleCapable controller) {
        this.controller = controller;
    }

    @Deprecated
    public void play(RumbleEffect effect) {
        play(RumbleSource.MASTER, effect);
    }

    public void play(RumbleSource source, RumbleEffect effect) {
        if (!controller.canRumble())
            return;

        playingEffect = new RumbleEffectInstance(source, effect);
    }

    public boolean isPlaying() {
        return playingEffect != null;
    }

    public void stopCurrentEffect() {
        if (playingEffect == null)
            return;

        controller.setRumble(0f, 0f, RumbleSource.MASTER);
        playingEffect = null;
    }

    public void tick() {
        if (playingEffect == null)
            return;

        if (playingEffect.effect().isFinished()) {
            stopCurrentEffect();
            return;
        }

        RumbleState state = playingEffect.effect().nextState();
        controller.setRumble(state.strong(), state.weak(), playingEffect.source());
    }

    private record RumbleEffectInstance(RumbleSource source, RumbleEffect effect) {
    }
}
