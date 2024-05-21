package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.controller.battery.BatteryLevel;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.input.JoystickInputs;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.controller.rumble.TriggerRumbleComponent;
import dev.isxander.controlify.controllermanager.UniqueControllerID;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.hid.HIDIdentifier;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.rumble.TriggerRumbleState;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.sdl3java.api.joystick.SDL_Joystick;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import dev.isxander.sdl3java.api.properties.SDL_PropertiesID;
import net.minecraft.util.Mth;

import java.util.Optional;
import java.util.Set;

import static dev.isxander.sdl3java.api.error.SdlError.SDL_GetError;
import static dev.isxander.sdl3java.api.joystick.SDL_JoystickPowerLevel.*;
import static dev.isxander.sdl3java.api.joystick.SdlJoystick.*;
import static dev.isxander.sdl3java.api.joystick.SdlJoystickHatConst.*;
import static dev.isxander.sdl3java.api.joystick.SdlJoystickPropsConst.SDL_PROP_JOYSTICK_CAP_RUMBLE_BOOLEAN;
import static dev.isxander.sdl3java.api.joystick.SdlJoystickPropsConst.SDL_PROP_JOYSTICK_CAP_TRIGGER_RUMBLE_BOOLEAN;
import static dev.isxander.sdl3java.api.properties.SdlProperties.SDL_GetBooleanProperty;

public class SDL3JoystickDriver implements Driver {
    private final SDL_Joystick ptrJoystick;
    private final ControllerEntity controller;

    private final boolean isRumbleSupported, isTriggerRumbleSupported;
    private final String guid;
    private final String name;

    private final int numAxes, numButtons, numHats;

    public SDL3JoystickDriver(SDL_JoystickID jid, ControllerType type, String uid, UniqueControllerID ucid, Optional<HIDIdentifier> hid) {
        this.ptrJoystick = SDL_OpenJoystick(jid);
        if (ptrJoystick == null)
            throw new IllegalStateException("Could not open joystick: " + SDL_GetError());

        SDL_PropertiesID props = SDL_GetJoystickProperties(ptrJoystick);

        this.guid = SDL_GetJoystickInstanceGUID(jid).toString();
        this.name = SDL_GetJoystickName(ptrJoystick);
        this.isRumbleSupported = SDL_GetBooleanProperty(props, SDL_PROP_JOYSTICK_CAP_RUMBLE_BOOLEAN, false);
        this.isTriggerRumbleSupported = SDL_GetBooleanProperty(props, SDL_PROP_JOYSTICK_CAP_TRIGGER_RUMBLE_BOOLEAN, false);

        ControllerInfo info = new ControllerInfo(uid, ucid, this.guid, this.name, type, hid);
        this.controller = new ControllerEntity(info);

        this.numAxes = SDL_GetNumJoystickAxes(ptrJoystick);
        this.numButtons = SDL_GetNumJoystickButtons(ptrJoystick);
        this.numHats = SDL_GetNumJoystickHats(ptrJoystick);

        this.controller.setComponent(new InputComponent(this.controller, numButtons, numAxes * 2, numHats, false, Set.of(), type.mappingId()), InputComponent.ID);
        this.controller.setComponent(new BatteryLevelComponent(), BatteryLevelComponent.ID);
        if (this.isRumbleSupported) {
            this.controller.setComponent(new RumbleComponent(), RumbleComponent.ID);
        }
        if (this.isTriggerRumbleSupported) {
            this.controller.setComponent(new TriggerRumbleComponent(), TriggerRumbleComponent.ID);
        }

        this.controller.finalise();
    }

    @Override
    public ControllerEntity getController() {
        return controller;
    }

    @Override
    public void update(boolean outOfFocus) {
        this.updateInput();
        this.updateRumble();
        this.updateBatteryLevel();
    }

    @Override
    public void close() {
        SDL_CloseJoystick(ptrJoystick);
    }

    private void updateInput() {
        ControllerStateImpl state = new ControllerStateImpl();

        for (int i = 0; i < numAxes; i++) {
            float axis = mapShortToFloat(SDL_GetJoystickAxis(ptrJoystick, i));

            if (i == 0) {
               // System.out.println(axis + " " + Math.max(axis, 0) + " " + -Math.min(axis, 0));
            }

            state.setAxis(JoystickInputs.axis(i, true), Math.max(axis, 0));
            state.setAxis(JoystickInputs.axis(i, false), -Math.min(axis, 0));
        }

        for (int i = 0; i < numButtons; i++) {
            state.setButton(JoystickInputs.button(i), SDL_GetJoystickButton(ptrJoystick, i) == 1);
        }

        for (int i = 0; i < numHats; i++) {
            HatState hatState = switch (SDL_GetJoystickHat(ptrJoystick, i)) {
                case SDL_HAT_CENTERED -> HatState.CENTERED;
                case SDL_HAT_UP -> HatState.UP;
                case SDL_HAT_RIGHT -> HatState.RIGHT;
                case SDL_HAT_DOWN -> HatState.DOWN;
                case SDL_HAT_LEFT -> HatState.LEFT;
                case SDL_HAT_RIGHTUP -> HatState.RIGHT_UP;
                case SDL_HAT_RIGHTDOWN -> HatState.RIGHT_DOWN;
                case SDL_HAT_LEFTUP -> HatState.LEFT_UP;
                case SDL_HAT_LEFTDOWN -> HatState.LEFT_DOWN;
                default -> throw new IllegalStateException("Unexpected value: " + SDL_GetJoystickHat(ptrJoystick, i));
            };

            state.setHat(JoystickInputs.hat(i), hatState);
        }

        this.controller.input().orElseThrow().pushState(state);
    }

    private void updateRumble() {
        if (isRumbleSupported) {
            Optional<RumbleState> stateOpt = this.controller
                    .rumble()
                    .orElseThrow()
                    .consumeRumble();

            stateOpt.ifPresent(state -> {
                if (SDL_RumbleJoystick(ptrJoystick, (short)(state.strong() * 0xFFFF), (short)(state.weak() * 0xFFFF), 0) != 0) {
                    CUtil.LOGGER.error("Could not rumble joystick: " + SDL_GetError());
                }
            });
        }

        if (isTriggerRumbleSupported) {
            Optional<TriggerRumbleState> stateOpt = this.controller
                    .triggerRumble()
                    .orElseThrow()
                    .consumeTriggerRumble();

            stateOpt.ifPresent(state -> {
                if (SDL_RumbleJoystickTriggers(ptrJoystick, (short)(state.left() * 0xFFFF), (short)(state.right() * 0xFFFF), 0) != 0) {
                    CUtil.LOGGER.error("Could not rumble triggers joystick: " + SDL_GetError());
                }
            });
        }
    }

    private void updateBatteryLevel() {
        BatteryLevel level = switch (SDL_GetJoystickPowerLevel(ptrJoystick)) {
            case SDL_JOYSTICK_POWER_UNKNOWN -> BatteryLevel.UNKNOWN;
            case SDL_JOYSTICK_POWER_EMPTY -> BatteryLevel.EMPTY;
            case SDL_JOYSTICK_POWER_LOW -> BatteryLevel.LOW;
            case SDL_JOYSTICK_POWER_MEDIUM -> BatteryLevel.MEDIUM;
            case SDL_JOYSTICK_POWER_FULL -> BatteryLevel.FULL;
            case SDL_JOYSTICK_POWER_WIRED -> BatteryLevel.WIRED;
            case SDL_JOYSTICK_POWER_MAX -> BatteryLevel.MAX;
            default -> throw new IllegalStateException("Unexpected value");
        };

        this.controller.batteryLevel().orElseThrow().setBatteryLevel(level);
    }

    private static float mapShortToFloat(short value) {
        return Mth.clampedMap(value, Short.MIN_VALUE, 0, -1f, 0f)
                + Mth.clampedMap(value, 0, Short.MAX_VALUE, 0f, 1f);
    }
}
