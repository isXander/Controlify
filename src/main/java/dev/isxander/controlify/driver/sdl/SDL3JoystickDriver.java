package dev.isxander.controlify.driver.sdl;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.input.JoystickInputs;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl3java.api.joystick.SDL_Joystick;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickConnectionState;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickGUID;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import dev.isxander.sdl3java.api.properties.SDL_PropertiesID;
import net.minecraft.util.Mth;

import java.util.Set;

import org.intellij.lang.annotations.MagicConstant;

import static dev.isxander.sdl3java.api.joystick.SdlJoystick.*;
import static dev.isxander.sdl3java.api.joystick.SdlJoystickHatConst.*;

public class SDL3JoystickDriver extends SDLCommonDriver<SDL_Joystick> {

    private InputComponent inputComponent;
    private final int numAxes, numButtons, numHats;

    public SDL3JoystickDriver(SDL_Joystick ptrJoystick, SDL_JoystickID jid, ControllerType type, ControlifyLogger logger) {
        super(ptrJoystick, jid, type, logger);

        this.numAxes = SDL_GetNumJoystickAxes(ptrJoystick);
        this.numButtons = SDL_GetNumJoystickButtons(ptrJoystick);
        this.numHats = SDL_GetNumJoystickHats(ptrJoystick);
    }

    @Override
    public void addComponents(ControllerEntity controller) {
        super.addComponents(controller);

        controller.setComponent(this.inputComponent = new InputComponent(controller, numButtons, numAxes * 2, numHats, false, Set.of(), controller.info().type().mappingId()));
    }

    @Override
    public void update(ControllerEntity controller, boolean outOfFocus) {
        super.update(controller, outOfFocus);

        this.updateInput();
    }

    private void updateInput() {
        ControllerStateImpl state = new ControllerStateImpl();

        for (int i = 0; i < numAxes; i++) {
            float axis = mapShortToFloat(SDL_GetJoystickAxis(ptrController, i));

            state.setAxis(JoystickInputs.axis(i, true), Math.max(axis, 0));
            state.setAxis(JoystickInputs.axis(i, false), -Math.min(axis, 0));
        }

        for (int i = 0; i < numButtons; i++) {
            state.setButton(JoystickInputs.button(i), SDL_GetJoystickButton(ptrController, i) == 1);
        }

        for (int i = 0; i < numHats; i++) {
            HatState hatState = switch (SDL_GetJoystickHat(ptrController, i)) {
                case SDL_HAT_CENTERED -> HatState.CENTERED;
                case SDL_HAT_UP -> HatState.UP;
                case SDL_HAT_RIGHT -> HatState.RIGHT;
                case SDL_HAT_DOWN -> HatState.DOWN;
                case SDL_HAT_LEFT -> HatState.LEFT;
                case SDL_HAT_RIGHTUP -> HatState.RIGHT_UP;
                case SDL_HAT_RIGHTDOWN -> HatState.RIGHT_DOWN;
                case SDL_HAT_LEFTUP -> HatState.LEFT_UP;
                case SDL_HAT_LEFTDOWN -> HatState.LEFT_DOWN;
                default -> throw new IllegalStateException("Unexpected value: " + SDL_GetJoystickHat(ptrController, i));
            };

            state.setHat(JoystickInputs.hat(i), hatState);
        }

        this.inputComponent.pushState(state);
    }

    private static float mapShortToFloat(short value) {
        return Mth.clampedMap(value, Short.MIN_VALUE, 0, -1f, 0f)
                + Mth.clampedMap(value, 0, Short.MAX_VALUE, 0f, 1f);
    }

    @Override
    protected SDL_PropertiesID SDL_GetControllerProperties(SDL_Joystick ptrController) {
        return SDL_GetJoystickProperties(ptrController);
    }

    @Override
    protected String SDL_GetControllerName(SDL_Joystick ptrController) {
        return SDL_GetJoystickName(ptrController);
    }

    @Override
    protected SDL_JoystickGUID SDL_GetControllerGUIDForID(SDL_JoystickID jid) {
        return SDL_GetJoystickGUIDForID(jid);
    }

    @Override
    protected String SDL_GetControllerSerial(SDL_Joystick ptrController) {
        return SDL_GetJoystickSerial(ptrController);
    }

    @Override
    protected short SDL_GetControllerVendor(SDL_Joystick ptrController) {
        return SDL_GetJoystickVendor(ptrController);
    }

    @Override
    protected short SDL_GetControllerProduct(SDL_Joystick ptrController) {
        return SDL_GetJoystickProduct(ptrController);
    }

    @Override
    protected int SDL_GetControllerConnectionState(SDL_Joystick ptrController) {
        return SDL_GetJoystickConnectionState(ptrController);
    }

    @Override
    protected boolean SDL_CloseController(SDL_Joystick ptrController) {
        SDL_CloseJoystick(ptrController);
        return true;
    }

    @Override
    protected boolean SDL_RumbleController(SDL_Joystick ptrController, float strong, float weak, int durationMs) {
        return SDL_RumbleJoystick(ptrController, (short) (strong * 0xFFFF), (short) (weak * 0xFFFF), durationMs);
    }

    @Override
    protected boolean SDL_RumbleControllerTriggers(SDL_Joystick ptrController, float left, float right, int durationMs) {
        return SDL_RumbleJoystickTriggers(ptrController, (short) (left * 0xFFFF), (short) (right * 0xFFFF), durationMs);
    }

    @Override
    protected int SDL_GetControllerPowerInfo(SDL_Joystick ptrController, IntByReference percent) {
        return SDL_GetJoystickPowerInfo(ptrController, percent);
    }

    @Override
    protected boolean SDL_SendControllerEffect(SDL_Joystick ptrController, Pointer effect, int size) {
        return SDL_SendJoystickEffect(ptrController, effect, size);
    }
}
