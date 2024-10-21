package dev.isxander.controlify.driver.steamdeck;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.controller.battery.PowerState;
import dev.isxander.controlify.controller.gyro.GyroComponent;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.keyboard.NativeKeyboardComponent;
import dev.isxander.controlify.controller.steamdeck.SteamDeckComponent;
import dev.isxander.controlify.controller.touchpad.TouchpadComponent;
import dev.isxander.controlify.controller.touchpad.Touchpads;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.deckapi.api.ControllerButton;
import dev.isxander.deckapi.api.ControllerState;
import dev.isxander.deckapi.api.SteamDeck;
import net.minecraft.client.Minecraft;
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
    private NativeKeyboardComponent keyboardComponent;

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

        // don't add - deck reports incorrect values
        this.batteryLevelComponent = new BatteryLevelComponent();

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

        controller.setComponent(
                this.keyboardComponent = new NativeKeyboardComponent(this::openKeyboard, 0.5f)
        );

        controller.setComponent(new SteamDeckComponent());
    }

    @Override
    public void update(ControllerEntity controller, boolean outOfFocus) {
        deck.poll().join();

        ControllerStateImpl state = new ControllerStateImpl();
        ControllerState deckState = deck.getControllerState();

        boolean focused = deck.isGameInFocus();
        Minecraft.getInstance().setWindowActive(focused);

        state.setButton(GamepadInputs.NORTH_BUTTON, deckState.getButtonState(ControllerButton.Y) && focused);
        state.setButton(GamepadInputs.EAST_BUTTON, deckState.getButtonState(ControllerButton.B) && focused);
        state.setButton(GamepadInputs.SOUTH_BUTTON, deckState.getButtonState(ControllerButton.A) && focused);
        state.setButton(GamepadInputs.WEST_BUTTON, deckState.getButtonState(ControllerButton.X) && focused);
        state.setButton(GamepadInputs.LEFT_SHOULDER_BUTTON, deckState.getButtonState(ControllerButton.L1) && focused);
        state.setButton(GamepadInputs.RIGHT_SHOULDER_BUTTON, deckState.getButtonState(ControllerButton.R1) && focused);
        state.setButton(GamepadInputs.START_BUTTON, deckState.getButtonState(ControllerButton.START) && focused);
        state.setButton(GamepadInputs.BACK_BUTTON, deckState.getButtonState(ControllerButton.SELECT) && focused);

        // not sure if this should ever be used, since it will always open the steam menu
//        state.setButton(GamepadInputs.GUIDE_BUTTON, deckState.getButtonState(ControllerButton.STEAM_HOME) && focused);

        state.setButton(GamepadInputs.LEFT_STICK_BUTTON, deckState.getButtonState(ControllerButton.L3) && focused);
        state.setButton(GamepadInputs.RIGHT_STICK_BUTTON, deckState.getButtonState(ControllerButton.R3) && focused);
        state.setButton(GamepadInputs.DPAD_UP_BUTTON, deckState.getButtonState(ControllerButton.DPAD_UP) && focused);
        state.setButton(GamepadInputs.DPAD_DOWN_BUTTON, deckState.getButtonState(ControllerButton.DPAD_DOWN) && focused);
        state.setButton(GamepadInputs.DPAD_LEFT_BUTTON, deckState.getButtonState(ControllerButton.DPAD_LEFT) && focused);
        state.setButton(GamepadInputs.DPAD_RIGHT_BUTTON, deckState.getButtonState(ControllerButton.DPAD_RIGHT) && focused);
        state.setButton(GamepadInputs.LEFT_PADDLE_1_BUTTON, deckState.getButtonState(ControllerButton.L4) && focused);
        state.setButton(GamepadInputs.LEFT_PADDLE_2_BUTTON, deckState.getButtonState(ControllerButton.L5) && focused);
        state.setButton(GamepadInputs.RIGHT_PADDLE_1_BUTTON, deckState.getButtonState(ControllerButton.R4) && focused);
        state.setButton(GamepadInputs.RIGHT_PADDLE_2_BUTTON, deckState.getButtonState(ControllerButton.R5) && focused);
        state.setButton(GamepadInputs.TOUCHPAD_1_BUTTON, deckState.getButtonState(ControllerButton.LEFT_TOUCHPAD_CLICK) && focused);
        state.setButton(GamepadInputs.TOUCHPAD_2_BUTTON, deckState.getButtonState(ControllerButton.RIGHT_TOUCHPAD_CLICK) && focused);
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_UP, zeroUnless(positiveAxis(mapShortToFloat(deckState.sLeftStickY())), focused));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_DOWN, zeroUnless(negativeAxis(mapShortToFloat(deckState.sLeftStickY())), focused));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_LEFT, zeroUnless(negativeAxis(mapShortToFloat(deckState.sLeftStickX())), focused));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_RIGHT, zeroUnless(positiveAxis(mapShortToFloat(deckState.sLeftStickX())), focused));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_UP, zeroUnless(positiveAxis(mapShortToFloat(deckState.sRightStickY())), focused));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_DOWN, zeroUnless(negativeAxis(mapShortToFloat(deckState.sRightStickY())), focused));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_LEFT, zeroUnless(negativeAxis(mapShortToFloat(deckState.sRightStickX())), focused));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_RIGHT, zeroUnless(positiveAxis(mapShortToFloat(deckState.sRightStickX())), focused));
        state.setAxis(GamepadInputs.LEFT_TRIGGER_AXIS, zeroUnless(mapShortToFloat(deckState.sTriggerL()), focused));
        state.setAxis(GamepadInputs.RIGHT_TRIGGER_AXIS, zeroUnless(mapShortToFloat(deckState.sTriggerR()), focused));

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

    private void openKeyboard() {
        deck.openModalKeyboard(true);
    }

    @Override
    public String getDriverName() {
        return "Steam Deck";
    }

    @Override
    public void close() {
        try {
            deck.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Optional<SteamDeckDriver> create() {
        if (triedToLoad)
            return Optional.empty();

        triedToLoad = true;

        try {
            deck = SteamDeckUtil.getDeckInstance().orElseThrow();
            return Optional.of(new SteamDeckDriver());
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<SteamDeck> getDeck() {
        return Optional.ofNullable(deck);
    }

    private static float zeroUnless(float f, boolean condition) {
        return condition ? f : 0;
    }
}
