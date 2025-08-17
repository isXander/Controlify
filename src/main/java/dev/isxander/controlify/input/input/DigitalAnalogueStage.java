package dev.isxander.controlify.input.input;

import dev.isxander.controlify.input.pipeline.EventSink;
import dev.isxander.controlify.input.pipeline.UnaryEventStage;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts digital button presses into analogue axis movements,
 * and converts analogue axis movements into button presses.
 * This allows the signal synthesizer to work with both types of inputs.
 */
public class DigitalAnalogueStage implements UnaryEventStage<InputEvent> {
    private final float actuation, release;
    private final float pressedValue, releasedValue;

    private final Map<ResourceLocation, Boolean> buttonStates = new HashMap<>();

    public DigitalAnalogueStage(
            float actuation, float release,
            float pressedValue, float releasedValue
    ) {
        this.actuation = actuation;
        this.release = release;
        this.pressedValue = pressedValue;
        this.releasedValue = releasedValue;
    }

    public DigitalAnalogueStage() {
        this(0.4f, 0.3f, 1.0f, 0.0f);
    }

    @Override
    public void onEvent(InputEvent event, EventSink<? super InputEvent> downstream) {
        downstream.accept(event);

        switch (event) {
            case InputEvent.AxisMoved(long timeNanos, ResourceLocation input, float value) -> {
                if (value >= actuation) {
                    if (!buttonStates.getOrDefault(input, false)) {
                        buttonStates.put(input, true);
                        downstream.accept(new InputEvent.ButtonPress(timeNanos, input));
                    }
                } else if (value <= release) {
                    if (buttonStates.getOrDefault(input, false)) {
                        buttonStates.put(input, false);
                        downstream.accept(new InputEvent.ButtonRelease(timeNanos, input));
                    }
                }
            }

            case InputEvent.ButtonPress(long timeNanos, ResourceLocation input) -> {
                downstream.accept(new InputEvent.AxisMoved(timeNanos, input, pressedValue));
            }

            case InputEvent.ButtonRelease(long timeNanos, ResourceLocation input) -> {
                downstream.accept(new InputEvent.AxisMoved(timeNanos, input, releasedValue));
            }

            default -> {}
        }
    }
}
