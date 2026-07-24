package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.input.JoystickInputs;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl.*;
import net.minecraft.util.Mth;

import java.nio.ByteBuffer;
import java.util.Set;

import static dev.isxander.sdl.SdlJoystick.*;

public class SDL3JoystickDriver extends SDLCommonDriver<SdlJoystickHandle> {

    private InputComponent inputComponent;
    private final int numAxes, numButtons, numHats;

    public SDL3JoystickDriver(Sdl sdl, SdlJoystickHandle ptrJoystick, SdlJoystickId jid, ControllerType type, ControlifyLogger logger) {
        super(sdl, ptrJoystick, jid, type, logger);

        this.numAxes = sdl.joystick().SDL_GetNumJoystickAxes(ptrJoystick);
        this.numButtons = sdl.joystick().SDL_GetNumJoystickButtons(ptrJoystick);
        this.numHats = sdl.joystick().SDL_GetNumJoystickHats(ptrJoystick);
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
            float axis = mapShortToFloat(sdl.joystick().SDL_GetJoystickAxis(ptrController, i));

            state.setAxis(JoystickInputs.axis(i, true), Math.max(axis, 0));
            state.setAxis(JoystickInputs.axis(i, false), -Math.min(axis, 0));
        }

        for (int i = 0; i < numButtons; i++) {
            state.setButton(JoystickInputs.button(i), sdl.joystick().SDL_GetJoystickButton(ptrController, i));
        }

        for (int i = 0; i < numHats; i++) {
            HatState hatState = switch (sdl.joystick().SDL_GetJoystickHat(ptrController, i)) {
                case SDL_HAT_CENTERED -> HatState.CENTERED;
                case SDL_HAT_UP -> HatState.UP;
                case SDL_HAT_RIGHT -> HatState.RIGHT;
                case SDL_HAT_DOWN -> HatState.DOWN;
                case SDL_HAT_LEFT -> HatState.LEFT;
                case SDL_HAT_RIGHTUP -> HatState.RIGHT_UP;
                case SDL_HAT_RIGHTDOWN -> HatState.RIGHT_DOWN;
                case SDL_HAT_LEFTUP -> HatState.LEFT_UP;
                case SDL_HAT_LEFTDOWN -> HatState.LEFT_DOWN;
                default -> throw new IllegalStateException("Unexpected value: " + sdl.joystick().SDL_GetJoystickHat(ptrController, i));
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
    protected SdlPropertiesId SDL_GetControllerProperties(SdlJoystickHandle ptrController) {
        return sdl.joystick().SDL_GetJoystickProperties(ptrController);
    }

    @Override
    protected String SDL_GetControllerName(SdlJoystickHandle ptrController) {
        return sdl.joystick().SDL_GetJoystickName(ptrController);
    }

    @Override
    protected SdlGuid SDL_GetControllerGUIDForID(SdlJoystickId jid) {
        return sdl.joystick().SDL_GetJoystickGUIDForID(jid);
    }

    @Override
    protected String SDL_GetControllerSerial(SdlJoystickHandle ptrController) {
        return sdl.joystick().SDL_GetJoystickSerial(ptrController);
    }

    @Override
    protected short SDL_GetControllerVendor(SdlJoystickHandle ptrController) {
        return sdl.joystick().SDL_GetJoystickVendor(ptrController);
    }

    @Override
    protected short SDL_GetControllerProduct(SdlJoystickHandle ptrController) {
        return sdl.joystick().SDL_GetJoystickProduct(ptrController);
    }

    @Override
    protected int SDL_GetControllerConnectionState(SdlJoystickHandle ptrController) {
        return sdl.joystick().SDL_GetJoystickConnectionState(ptrController);
    }

    @Override
    protected boolean SDL_CloseController(SdlJoystickHandle ptrController) {
		sdl.joystick().SDL_CloseJoystick(ptrController);
        return true;
    }

    @Override
    protected boolean SDL_RumbleController(SdlJoystickHandle ptrController, float strong, float weak, int durationMs) {
        return sdl.joystick().SDL_RumbleJoystick(ptrController, (short) (strong * 0xFFFF), (short) (weak * 0xFFFF), durationMs);
    }

    @Override
    protected boolean SDL_RumbleControllerTriggers(SdlJoystickHandle ptrController, float left, float right, int durationMs) {
        return sdl.joystick().SDL_RumbleJoystickTriggers(ptrController, (short) (left * 0xFFFF), (short) (right * 0xFFFF), durationMs);
    }

    @Override
    protected int SDL_GetControllerPowerInfo(SdlJoystickHandle ptrController, SdlRefs.IntRef percent) {
        return sdl.joystick().SDL_GetJoystickPowerInfo(ptrController, percent);
    }

    @Override
    protected boolean SDL_SendControllerEffect(SdlJoystickHandle ptrController, ByteBuffer effect) {
        return sdl.joystick().SDL_SendJoystickEffect(ptrController, effect);
    }

    @Override
    protected boolean SDL_SetControllerLED(SdlJoystickHandle ptrController, byte red, byte green, byte blue) {
        return sdl.joystick().SDL_SetJoystickLED(ptrController, red, green, blue);
    }
}
