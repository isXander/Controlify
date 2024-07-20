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
import dev.isxander.controlify.controller.touchpad.TouchpadState;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.deckapi.api.ControllerButton;
import dev.isxander.deckapi.api.ControllerState;
import dev.isxander.deckapi.api.SteamDeck;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

import java.util.List;
import java.util.Optional;

public class SteamDeckDriver implements Driver {
    private final SteamDeck deck;
    private static boolean triedToLoad = false;

    private InputComponent inputComponent;
    private GyroComponent gyroComponent;
    private BatteryLevelComponent batteryLevelComponent;
    private TouchpadComponent touchpadComponent;

    public SteamDeckDriver(SteamDeck deck) {
        this.deck = deck;
    }

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
                this.touchpadComponent = new TouchpadComponent(2) // there are two touchpads, one for each thumb
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
        state.setButton(GamepadInputs.TOUCHPAD_BUTTON,
                deckState.getButtonState(ControllerButton.LEFT_TOUCHPAD_CLICK) || deckState.getButtonState(ControllerButton.RIGHT_TOUCHPAD_TOUCH));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_UP, negativeAxis(mapShortToFloat(deckState.sLeftStickY())));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_DOWN, positiveAxis(mapShortToFloat(deckState.sLeftStickY())));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_LEFT, negativeAxis(mapShortToFloat(deckState.sLeftStickX())));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_RIGHT, positiveAxis(mapShortToFloat(deckState.sLeftStickX())));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_UP, negativeAxis(mapShortToFloat(deckState.sRightStickY())));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_DOWN, positiveAxis(mapShortToFloat(deckState.sRightStickY())));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_LEFT, negativeAxis(mapShortToFloat(deckState.sRightStickX())));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_RIGHT, positiveAxis(mapShortToFloat(deckState.sRightStickX())));
        state.setAxis(GamepadInputs.LEFT_TRIGGER_AXIS, mapShortToFloat(deckState.sTriggerL()));
        state.setAxis(GamepadInputs.RIGHT_TRIGGER_AXIS, mapShortToFloat(deckState.sTriggerR()));

        this.inputComponent.pushState(state);

        // pitch yaw roll
        this.gyroComponent.setState(
                new GyroState(
                        deckState.flGyroDegreesPerSecondX(),
                        deckState.flGyroDegreesPerSecondY(),
                        deckState.flGyroDegreesPerSecondZ()
                )
        );

        this.batteryLevelComponent.setBatteryLevel(
                new PowerState.Depleting((int) (mapShortToFloat(deckState.sBatteryLevel()) * 100))
        );

        this.touchpadComponent.pushFingers(
                List.of(
                        new TouchpadState.Finger(
                                new Vector2f(
                                        mapShortToFloat(deckState.sLeftPadX()),
                                        mapShortToFloat(deckState.sLeftPadY())
                                ),
                                mapShortToFloat(deckState.sPressurePadLeft())
                        ),
                        new TouchpadState.Finger(
                                new Vector2f(
                                        mapShortToFloat(deckState.sRightPadX()),
                                        mapShortToFloat(deckState.sRightPadY())
                                ),
                                mapShortToFloat(deckState.sPressurePadRight())
                        )
                )
        );
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
            SteamDeck deck = SteamDeck.create();
            return Optional.of(new SteamDeckDriver(deck));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static float positiveAxis(float value) {
        return value < 0 ? 0 : value;
    }

    private static float negativeAxis(float value) {
        return value > 0 ? 0 : -value;
    }

    private static float mapShortToFloat(short value) {
        // we need to do this since signed short range / 2 != 0
        return Mth.clampedMap(value, Short.MIN_VALUE, 0, -1f, 0f)
                + Mth.clampedMap(value, 0, Short.MAX_VALUE, 0f, 1f);
    }
}
