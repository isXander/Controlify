package dev.isxander.controlify.driver.steamdeck;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.controller.battery.PowerState;
import dev.isxander.controlify.controller.gyro.GyroComponent;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.touchpad.TouchpadComponent;
import dev.isxander.controlify.controller.touchpad.Touchpads;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.deckapi.api.ControllerButton;
import dev.isxander.deckapi.api.ControllerState;
import dev.isxander.deckapi.api.SteamDeck;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

import java.util.List;
import java.util.Optional;

import static dev.isxander.controlify.utils.CUtil.*;

public class SteamDeckDriver implements Driver {
    private static SteamDeck deck;
    private static boolean triedToLoad = false;

    private InputComponent inputComponent;
    private GyroComponent gyroComponent;
    private BatteryLevelComponent batteryLevelComponent;
    private TouchpadComponent touchpadComponent;

    @Override
    public void addComponents(ControllerEntity controller) {
        controller.setComponent(
                this.inputComponent = new InputComponent(
                        controller,
                        20,
                        10,
                        0,
                        true,
                        GamepadInputs.DEADZONE_GROUPS,
                        controller.info().type().mappingId()
                )
        );

        controller.setComponent(
                this.gyroComponent = new GyroComponent()
        );

        controller.setComponent(
                this.batteryLevelComponent = new BatteryLevelComponent()
        );

        controller.setComponent(
                this.touchpadComponent = new TouchpadComponent(
                        new Touchpads(
                                new Touchpads.Touchpad[]{
                                        new Touchpads.Touchpad(1), // left
                                        new Touchpads.Touchpad(1), // right
                                }
                        )
                ) // there are two touchpads, one for each thumb
        );
    }

    @Override
    public void update(ControllerEntity controller, boolean outOfFocus) {
        deck.poll().join();

        ControllerStateImpl state = new ControllerStateImpl();
        ControllerState deckState = deck.getControllerState();

        state.setButton(GamepadInputs.NORTH_BUTTON, deckState.getButtonState(ControllerButton.Y));
        state.setButton(GamepadInputs.EAST_BUTTON, deckState.getButtonState(ControllerButton.B));
        state.setButton(GamepadInputs.SOUTH_BUTTON, deckState.getButtonState(ControllerButton.A));
        state.setButton(GamepadInputs.WEST_BUTTON, deckState.getButtonState(ControllerButton.X));
        state.setButton(GamepadInputs.LEFT_SHOULDER_BUTTON, deckState.getButtonState(ControllerButton.L1));
        state.setButton(GamepadInputs.RIGHT_SHOULDER_BUTTON, deckState.getButtonState(ControllerButton.R1));
        state.setButton(GamepadInputs.START_BUTTON, deckState.getButtonState(ControllerButton.START));
        state.setButton(GamepadInputs.BACK_BUTTON, deckState.getButtonState(ControllerButton.SELECT));
        state.setButton(GamepadInputs.GUIDE_BUTTON, deckState.getButtonState(ControllerButton.STEAM_HOME));
        state.setButton(GamepadInputs.LEFT_STICK_BUTTON, deckState.getButtonState(ControllerButton.L3));
        state.setButton(GamepadInputs.RIGHT_STICK_BUTTON, deckState.getButtonState(ControllerButton.R3));
        state.setButton(GamepadInputs.DPAD_UP_BUTTON, deckState.getButtonState(ControllerButton.DPAD_UP));
        state.setButton(GamepadInputs.DPAD_DOWN_BUTTON, deckState.getButtonState(ControllerButton.DPAD_DOWN));
        state.setButton(GamepadInputs.DPAD_LEFT_BUTTON, deckState.getButtonState(ControllerButton.DPAD_LEFT));
        state.setButton(GamepadInputs.DPAD_RIGHT_BUTTON, deckState.getButtonState(ControllerButton.DPAD_RIGHT));
        state.setButton(GamepadInputs.LEFT_PADDLE_1_BUTTON, deckState.getButtonState(ControllerButton.L4));
        state.setButton(GamepadInputs.LEFT_PADDLE_2_BUTTON, deckState.getButtonState(ControllerButton.L5));
        state.setButton(GamepadInputs.RIGHT_PADDLE_1_BUTTON, deckState.getButtonState(ControllerButton.R4));
        state.setButton(GamepadInputs.RIGHT_PADDLE_2_BUTTON, deckState.getButtonState(ControllerButton.R5));
        state.setButton(GamepadInputs.TOUCHPAD_1_BUTTON, deckState.getButtonState(ControllerButton.LEFT_TOUCHPAD_CLICK));
        state.setButton(GamepadInputs.TOUCHPAD_2_BUTTON, deckState.getButtonState(ControllerButton.RIGHT_TOUCHPAD_CLICK));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_UP, positiveAxis(mapShortToFloat(deckState.sLeftStickY())));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_DOWN, negativeAxis(mapShortToFloat(deckState.sLeftStickY())));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_LEFT, negativeAxis(mapShortToFloat(deckState.sLeftStickX())));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_RIGHT, positiveAxis(mapShortToFloat(deckState.sLeftStickX())));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_UP, positiveAxis(mapShortToFloat(deckState.sRightStickY())));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_DOWN, negativeAxis(mapShortToFloat(deckState.sRightStickY())));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_LEFT, negativeAxis(mapShortToFloat(deckState.sRightStickX())));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_RIGHT, positiveAxis(mapShortToFloat(deckState.sRightStickX())));
        state.setAxis(GamepadInputs.LEFT_TRIGGER_AXIS, mapShortToFloat(deckState.sTriggerL()));
        state.setAxis(GamepadInputs.RIGHT_TRIGGER_AXIS, mapShortToFloat(deckState.sTriggerR()));

        this.inputComponent.pushState(state);

        this.gyroComponent.setState(
                new GyroState(
                        deckState.flSoftwareGyroDegreesPerSecondPitch(),
                        -deckState.flSoftwareGyroDegreesPerSecondYaw(),
                        -deckState.flSoftwareGyroDegreesPerSecondRoll()
                ).mul(Mth.DEG_TO_RAD) // we need radians per second, not degrees
        );

        this.batteryLevelComponent.setBatteryLevel(
                new PowerState.Depleting((int) (mapShortToFloat(deckState.sBatteryLevel()) * 100))
        );

        updateTouchpad(
                this.touchpadComponent.touchpads()[0],
                deckState.sLeftPadX(),
                deckState.sLeftPadY(),
                deckState.sPressurePadLeft(),
                deckState.getButtonState(ControllerButton.LEFT_TOUCHPAD_TOUCH)
        );
        updateTouchpad(
                this.touchpadComponent.touchpads()[1],
                deckState.sRightPadX(),
                deckState.sRightPadY(),
                deckState.sPressurePadRight(),
                deckState.getButtonState(ControllerButton.RIGHT_TOUCHPAD_TOUCH)
        );
    }

    private void updateTouchpad(Touchpads.Touchpad touchpad, short x, short y, short pressure, boolean touching) {
        if (touching) {
            // steam deck touchpad is -32768 to 32767, we need to map it to 0 to 1
            // origin [0,0] is middle and [1,1] is bottom right, we need to map it to top left
            float mappedX = (mapShortToFloat(x) + 1) / 2f;
            float mappedY = 1 - (mapShortToFloat(y) + 1) / 2f;
            // pressure is still 0 up until actuation point (click)
            float mappedPressure = mapShortToFloat(pressure); // [0, 1]

            touchpad.pushFingers(
                    List.of(
                            new Touchpads.Finger(0, new Vector2f(mappedX, mappedY), mappedPressure)
                    )
            );
        } else {
            touchpad.pushFingers(List.of());
        }
    }

    @Override
    public String getDriverName() {
        return "Steam Deck";
    }

    @Override
    public void close() {
        try {
            this.deck.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Optional<SteamDeckDriver> create() {
        if (triedToLoad)
            return Optional.empty();

        triedToLoad = true;

        try {
            deck = SteamDeck.create();
            return Optional.of(new SteamDeckDriver());
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<SteamDeck> getDeck() {
        return Optional.ofNullable(deck);
    }
}
