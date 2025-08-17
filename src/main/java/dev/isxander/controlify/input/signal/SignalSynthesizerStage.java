package dev.isxander.controlify.input.signal;

import dev.isxander.controlify.input.input.InputEvent;
import dev.isxander.controlify.input.pipeline.EventSink;
import dev.isxander.controlify.input.pipeline.EventStage;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * An event stage that synthesizes {@link InputEvent input events} into {@link Signal signals}.
 */
public class SignalSynthesizerStage implements EventStage<InputEvent, Signal> {

    // parameters for synthesizing signals
    public record Config(
            long tapMaxNs,
            long doubleTapWindowNs,
            long holdNs
    ) {
        public static final Config DEFAULT = new Config(
                ms(180), // tapMaxNs
                ms(220), // doubleTapWindowNs
                ms(350) // holdNs
        );

        private static long ms(long ms) {
            return ms * 1_000_000L;
        }
    }

    private final Config config;

    // count gui navigations
    private long navEpoch;
    public void onGuiNavigation() {
        this.navEpoch++;
    }

    private final Map<ResourceLocation, ButtonTracker> buttons = new HashMap<>();
    private final Map<ResourceLocation, AxisTracker> axes = new HashMap<>();

    public SignalSynthesizerStage() {
        this(Config.DEFAULT);
    }
    public SignalSynthesizerStage(Config config) {
        this.config = config;
    }

    @Override
    public void onEvent(InputEvent event, EventSink<? super Signal> downstream) {
        switch (event) {
            case InputEvent.ButtonPress(long timeNanos, ResourceLocation btn) ->
                    btn(btn).onPress(timeNanos, downstream);

            case InputEvent.ButtonRelease(long timeNanos, ResourceLocation btn) ->
                    btn(btn).onRelease(timeNanos, downstream);

            case InputEvent.AxisMoved(long timeNanos, ResourceLocation axis, float value) ->
                    axis(axis).onMove(timeNanos, value, downstream);

            case InputEvent.Tick(long timeNanos) -> {
                this.buttons.values().forEach(t -> t.onTick(timeNanos, downstream));
                this.axes.values().forEach(t -> t.onTick(timeNanos, downstream));

                downstream.accept(new Signal.Tick(timeNanos));
            }
        }
    }

    private ButtonTracker btn(ResourceLocation id) {
        return buttons.computeIfAbsent(id, ButtonTracker::new);
    }

    private AxisTracker axis(ResourceLocation id) {
        return axes.computeIfAbsent(id, AxisTracker::new);
    }

    private final class ButtonTracker {
        private enum State { IDLE, DOWN_1, UP_1_PENDING, DOWN_2 }

        private final ResourceLocation id;

        private State state = State.IDLE;
        private long timeDown1, timeUp1, timeDown2;
        private boolean heldEmitted = false;

        // GUI-press bookkeeping
        private long pressNavEpoch = 0;

        private ButtonTracker(ResourceLocation id) { this.id = id; }

        private void onPress(long time, EventSink<? super Signal> downstream) {
            switch (state) {
                case IDLE -> {
                    state = State.DOWN_1;
                    timeDown1 = time;
                    heldEmitted = false;
                    pressNavEpoch = navEpoch; // capture epoch for GUI-press
                    downstream.accept(new Signal.ButtonDown(time, id));
                }
                case UP_1_PENDING -> {
                    state = State.DOWN_2;
                    timeDown2 = time;
                    downstream.accept(new Signal.ButtonDown(time, id));
                }
                case DOWN_1, DOWN_2 -> {
                    CUtil.LOGGER.warn("Received press event but button {} is already pressed. Faulty input stage?", this.id);
                }
            }
        }

        private void onRelease(long time, EventSink<? super Signal> downstream) {
            switch (state) {
                case DOWN_1 -> {
                    downstream.accept(new Signal.ButtonUp(time, id));
                    long downDuration = time - timeDown1;

                    // GUI press: must be "tap-like" and no navigation in between
                    if (downDuration <= config.tapMaxNs && pressNavEpoch == navEpoch) {
                        downstream.accept(new Signal.GuiPress(time, id));
                    }

                    // Single-tap candidate?
                    if (!heldEmitted && downDuration <= config.tapMaxNs) {
                        // Defer tap until we know it’s not a double
                        state = State.UP_1_PENDING;
                        timeUp1 = time;
                    } else {
                        // Was a hold/slow press → end
                        state = State.IDLE;
                    }
                }
                case DOWN_2 -> {
                    downstream.accept(new Signal.ButtonUp(time, id));
                    long upToUp = time - timeUp1;
                    if (upToUp <= config.doubleTapWindowNs) {
                        downstream.accept(new Signal.DoubleTapped(time, id));
                    } else {
                        // If too slow, we treat as TAP then TAP (first will be finalized by timeout).
                        // Second tap will be handled by the next DOWN_1/UP path (here we just end).
                    }
                    state = State.IDLE;
                }
                case IDLE, UP_1_PENDING -> {
                    // spurious release; ignore
                }
            }
        }

        // TODO: wire up to an InputEvent to keep it within the pipeline
        private void onTick(long time, EventSink<? super Signal> out) {
            // Emit Held exactly once when threshold is crossed
            if (state == State.DOWN_1 && !heldEmitted) {
                long holdDuration = time - timeDown1;
                if (holdDuration >= config.holdNs) {
                    heldEmitted = true;
                    out.accept(new Signal.Held(time, id));
                    // NOTE: by policy, a hold cancels subsequent Tap/DoubleTap for this press cycle.
                }
            }

            // Resolve pending single tap if the double window has expired
            if (state == State.UP_1_PENDING) {
                if (time - timeUp1 >= config.doubleTapWindowNs) {
                    out.accept(new Signal.Tapped(timeUp1, id)); // timestamp at release time
                    state = State.IDLE;
                }
            }
        }
    }

    // ===== Axis tracker: coalesce + digitalize with hysteresis =====

    private final class AxisTracker {
        private final ResourceLocation id;

        private float lastValue = 0f;     // post-deadzone
        private float pendingValue = 0f;  // last raw seen (post-deadzone)
        private boolean dirty = false;

        private AxisTracker(ResourceLocation id) { this.id = id; }

        private void onMove(long time, float value, EventSink<? super Signal> downstream) {
            // Coalesce: keep only the latest value/time per tick
            pendingValue = value;
            dirty = true;
        }

        private void onTick(long time, EventSink<? super Signal> downstream) {
            if (!dirty) return; // nothing to do
            dirty = false;

            if (Math.abs(pendingValue - lastValue) >= 0.001f) { // Hysteresis threshold
                lastValue = pendingValue;
                downstream.accept(new Signal.AxisMoved(time, id, lastValue));
            }
        }
    }
}
