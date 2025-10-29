package dev.isxander.controlify.input.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.input.pipeline.EventSink;
import dev.isxander.controlify.input.pipeline.UnaryEventStage;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Converts {@link InputEvent.ButtonChanged} events into {@link InputEvent.AxisMoved} events,
 * and vice versa.
 * <p>
 * These synthesized input events use the same time and input identifier, meaning
 * both a {@link InputEvent.ButtonChanged} and an {@link InputEvent.AxisMoved} may
 * reference the same input.
 * <p>
 * This allows the signal synthesizer to work with both types of inputs.
 */
public class DigitalAnalogueStage implements UnaryEventStage<InputEvent> {
    private Supplier<Config> config;

    private final Map<ResourceLocation, Boolean> buttonStates = new HashMap<>();

    public DigitalAnalogueStage(Supplier<Config> config) {
        this.config = config;
    }

    public DigitalAnalogueStage(
            float actuation, float release,
            float pressedValue, float releasedValue
    ) {
        this(() -> new Config(actuation, release, pressedValue, releasedValue));
    }

    public DigitalAnalogueStage() {
        this(() -> Config.DEFAULT);
    }

    @Override
    public void onEvent(InputEvent event, EventSink<? super InputEvent> downstream) {
        downstream.accept(event);

        var config = this.config.get();

        switch (event) {
            case InputEvent.AxisMoved(long timeNanos, ResourceLocation input, float value) -> {
                if (value >= config.actuation) {
                    if (!buttonStates.getOrDefault(input, false)) {
                        buttonStates.put(input, true);
                        downstream.accept(new InputEvent.ButtonChanged(timeNanos, input, true));
                    }
                } else if (value <= config.release) {
                    if (buttonStates.getOrDefault(input, false)) {
                        buttonStates.put(input, false);
                        downstream.accept(new InputEvent.ButtonChanged(timeNanos, input, false));
                    }
                }
            }

            case InputEvent.ButtonChanged(long timeNanos, ResourceLocation input, boolean state) -> {
                downstream.accept(new InputEvent.AxisMoved(timeNanos, input, state ? config.pressedValue : config.releasedValue));
            }

            default -> {}
        }
    }

    public void setConfigSupplier(Supplier<Config> config) {
        this.config = config;
    }

    public record Config(float actuation, float release, float pressedValue, float releasedValue) {
        public static final Config DEFAULT = new Config(0.4f, 0.3f, 1.0f, 0.0f);

        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("actuation").forGetter(Config::actuation),
                Codec.FLOAT.fieldOf("release").forGetter(Config::release),
                Codec.FLOAT.fieldOf("pressed_value").forGetter(Config::pressedValue),
                Codec.FLOAT.fieldOf("released_value").forGetter(Config::releasedValue)
        ).apply(instance, Config::new));
    }
}
