package dev.isxander.controlify.rumble;

public class RumbleManager {
    private final RumbleCapable controller;
    private RumbleEffect playingEffect;
    private int currentPlayingTick;

    public RumbleManager(RumbleCapable controller) {
        this.controller = controller;
    }

    public void play(RumbleEffect effect) {
        if (!controller.canRumble())
            return;

        playingEffect = effect;
        currentPlayingTick = 0;
    }

    public boolean isPlaying() {
        return playingEffect != null;
    }

    public void stopCurrentEffect() {
        if (playingEffect == null)
            return;

        controller.setRumble(0f, 0f);
        playingEffect = null;
        currentPlayingTick = 0;
    }

    public void tick() {
        if (playingEffect == null)
            return;

        if (currentPlayingTick >= playingEffect.states().length) {
            stopCurrentEffect();
            return;
        }

        RumbleState state = playingEffect.states()[currentPlayingTick];
        controller.setRumble(state.strong(), state.weak());
        currentPlayingTick++;
    }
}
