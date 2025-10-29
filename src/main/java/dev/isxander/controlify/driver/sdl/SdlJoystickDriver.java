package dev.isxander.controlify.driver.sdl;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.input.JoystickInputs;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.input.InputComponent;
import dev.isxander.controlify.input.InputPipeline;
import dev.isxander.controlify.input.SensorPipeline;
import dev.isxander.controlify.input.pipeline.Clock;
import dev.isxander.controlify.input.pipeline.EventSource;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl3java.api.joystick.SDL_Joystick;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickGUID;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import dev.isxander.sdl3java.api.properties.SDL_PropertiesID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.HashSet;
import java.util.Set;

import static dev.isxander.sdl3java.api.joystick.SdlJoystick.*;

public class SdlJoystickDriver extends SdlCommonDriver<SDL_Joystick> {

    private InputComponent inputComponent;

    public SdlJoystickDriver(SDL_Joystick ptrJoystick, SDL_JoystickID jid, ControllerType type, EventSource<SdlControllerEvent> eventSource, ControlifyLogger logger) {
        super(ptrJoystick, jid, type, eventSource, logger);
    }

    @Override
    public void addComponents(ControllerEntity controller) {
        super.addComponents(controller);

        controller.setComponent(
                this.inputComponent = new InputComponent(
                        controller,
                        new InputPipeline(this.eventSource.via(new SdlInputEventStage()), Clock.STOPPED), // TODO: Clock
                        new SensorPipeline(this.eventSource.via(new SdlSensorEventStage()), Clock.STOPPED), // TODO: Clock
                        generateButtonSet(this.ptrController),
                        generateAxisSet(this.ptrController),
                        Set.of(),
                        false,
                        Set.of()
                )
        );
    }

    private Set<ResourceLocation> generateButtonSet(SDL_Joystick ptrJoystick) {
        int numButtons = SDL_GetNumJoystickButtons(ptrJoystick);
        var set = HashSet.<ResourceLocation>newHashSet(numButtons);
        for (int i = 0; i < numButtons; i++) {
            set.add(JoystickInputs.button(i));
        }
        return set;
    }

    private Set<ResourceLocation> generateAxisSet(SDL_Joystick ptrJoystick) {
        int numAxes = SDL_GetNumJoystickAxes(ptrJoystick);
        var set = HashSet.<ResourceLocation>newHashSet(numAxes * 2);
        for (int i = 0; i < numAxes; i++) {
            set.add(JoystickInputs.axis(i, true));
            set.add(JoystickInputs.axis(i, false));
        }
        return set;
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

    @Override
    protected boolean SDL_SetControllerLED(SDL_Joystick ptrController, byte red, byte green, byte blue) {
        return SDL_SetJoystickLED(ptrController, red, green, blue);
    }
}
