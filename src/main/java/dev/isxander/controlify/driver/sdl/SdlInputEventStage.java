package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.controller.input.JoystickInputs;
import dev.isxander.controlify.input.input.InputEvent;
import dev.isxander.controlify.input.pipeline.EventSink;
import dev.isxander.controlify.input.pipeline.EventStage;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

import static dev.isxander.sdl3java.api.gamepad.SDL_GamepadAxis.*;

public class SdlInputEventStage implements EventStage<SdlControllerEvent, InputEvent> {
    private final float[] prevGamepadAxisValues = new float[SDL_GAMEPAD_AXIS_COUNT];
    private final float[] prevJoystickAxisValues = new float[64];

    public SdlInputEventStage() {
        Arrays.fill(this.prevGamepadAxisValues, 0f);
        Arrays.fill(this.prevJoystickAxisValues, 0f);
    }

    @Override
    public void onEvent(SdlControllerEvent event, EventSink<? super InputEvent> downstream) {
        switch (event) {
            case SdlControllerEvent.JoyButton jb -> {
                ResourceLocation input = JoystickInputs.button(jb.button());
                downstream.accept(new InputEvent.ButtonChanged(jb.timestamp(), input, jb.down()));
            }

            case SdlControllerEvent.GamepadButton gb -> {
                ResourceLocation input = SdlInputConversions.mapGamepadButton(gb.button());
                downstream.accept(new InputEvent.ButtonChanged(gb.timestamp(), input, gb.down()));
            }

            case SdlControllerEvent.JoyAxis ja -> {
                float normalisedValue = normalise(ja.value());

                float prevNormalisedValue = this.prevJoystickAxisValues[ja.axis()];
                this.prevJoystickAxisValues[ja.axis()] = normalisedValue;

                boolean positive = normalisedValue > 0;
                boolean prevPositive = prevNormalisedValue > 0;

                if (prevPositive != positive) {
                    // sign changed
                    ResourceLocation prevInput = JoystickInputs.axis(ja.axis(), prevPositive);
                    downstream.accept(new InputEvent.AxisMoved(ja.timestamp(), prevInput, 0f));
                }

                ResourceLocation input = JoystickInputs.axis(ja.axis(), positive);
                downstream.accept(new InputEvent.AxisMoved(ja.timestamp(), input, Math.abs(normalisedValue)));
            }

            case SdlControllerEvent.GamepadAxis ga -> {
                float normalisedValue = normalise(ga.value());

                float prevNormalisedValue = this.prevGamepadAxisValues[ga.axis()];
                this.prevGamepadAxisValues[ga.axis()] = normalisedValue;

                boolean positive = normalisedValue > 0;
                boolean prevPositive = prevNormalisedValue > 0;

                if (prevPositive != positive) {
                    // sign changed
                    ResourceLocation prevInput = SdlInputConversions.mapGamepadAxis(ga.axis(), prevPositive);
                    downstream.accept(new InputEvent.AxisMoved(ga.timestamp(), prevInput, 0f));
                }

                ResourceLocation input = SdlInputConversions.mapGamepadAxis(ga.axis(), positive);
                downstream.accept(new InputEvent.AxisMoved(ga.timestamp(), input, Math.abs(normalisedValue)));
            }

            default -> {}
        }
    }

    private static float normalise(short v) {
        return v < 0 ? (v / 32768f) : (v / 32767f);
    }
}
