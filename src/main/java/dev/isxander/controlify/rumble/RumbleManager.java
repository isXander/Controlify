package dev.isxander.controlify.rumble;

public class RumbleManager {
    private final RumbleCapable controller;
    private RumbleEffect playingEffect;

    public RumbleManager(RumbleCapable controller) {
        this.controller = controller;
    }

    public void play(RumbleEffect effect) {
        if (!controller.canRumble())
            return;

        playingEffect = effect;
    }

    public boolean isPlaying() {
        return playingEffect != null;
    }

    public void stopCurrentEffect() {
        if (playingEffect == null)
            return;

        controller.setRumble(0f, 0f);
        playingEffect = null;
    }

    public void tick() {
        if (playingEffect == null)
            return;

        if (playingEffect.isFinished()) {
            stopCurrentEffect();
            return;
        }

        RumbleState state = playingEffect.nextState();
        controller.setRumble(state.strong(), state.weak());
    }
}
