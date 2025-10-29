package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.input.pipeline.EventSink;
import dev.isxander.controlify.input.pipeline.EventStage;
import dev.isxander.sdl3java.api.events.SdlEventTypes;
import dev.isxander.sdl3java.api.gamepad.SDL_GamepadAxis;
import dev.isxander.sdl3java.api.gamepad.SDL_GamepadButton;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import dev.isxander.sdl3java.api.joystick.SdlJoystickHatConst;
import dev.isxander.sdl3java.api.power.SDL_PowerState;
import dev.isxander.sdl3java.api.sensor.SDL_SensorType;
import org.intellij.lang.annotations.MagicConstant;

import static dev.isxander.sdl3java.api.events.SDL_EventType.*;
import static dev.isxander.sdl3java.api.power.SDL_PowerState.*;

public sealed interface SdlControllerEvent {

    /**
     * The timestamp of the event, in nanoseconds.
     * @return The timestamp of the event.
     */
    long timestamp();

    /**
     * The joystick associated with the event.
     */
    SDL_JoystickID which();

    sealed interface JoystickEvent extends SdlControllerEvent {}
    sealed interface GamepadEvent extends SdlControllerEvent {}

    sealed interface DeviceEvent extends SdlControllerEvent {
        Type type();

        enum Type {
            ADDED,
            REMOVED,
            UPDATE_COMPLETE,
            REMAPPED, // gamepad only
            STEAM_HANDLE_UPDATED, // gamepad only
        }
    }

    sealed interface AxisEvent extends SdlControllerEvent {
        /**
         * The axis. It may be an index or a constant, depending on the event type.
         */
        byte axis();

        /**
         * The axis value, in the range [-32768, 32767].
         */
        short value();
    }

    sealed interface ButtonEvent extends SdlControllerEvent {
        /**
         * The button. It may be an index or a constant, depending on the event type.
         */
        byte button();

        /**
         * True if the button is pressed, false if released.
         */
        boolean down();
    }

    /**
     * A joystick device event.
     * @param which the joystick ID
     * @param timestamp the event timestamp, in nanoseconds
     * @param type the type of device event
     */
    record JoyDevice(
            SDL_JoystickID which,
            long timestamp,
            Type type
    ) implements JoystickEvent, DeviceEvent {}

    /**
     * A joystick axis motion event.
     * @param which the joystick ID
     * @param timestamp the event timestamp, in nanoseconds
     * @param axis the axis index
     * @param value the axis value, in the range [-32768, 32767]
     */
    record JoyAxis(
            SDL_JoystickID which,
            long timestamp,
            byte axis,
            short value
    ) implements JoystickEvent, AxisEvent {}

    /**
     * A joystick trackball motion event.
     * @param which the joystick ID
     * @param timestamp the event timestamp, in nanoseconds
     * @param ball the trackball index
     * @param xrel the relative motion in the X direction
     * @param yrel the relative motion in the Y direction
     */
    record JoyBall(
            SDL_JoystickID which,
            long timestamp,
            byte ball,
            short xrel,
            short yrel
    ) implements JoystickEvent {}

    /**
     * A joystick hat motion event.
     * @param which the joystick ID
     * @param timestamp the event timestamp, in nanoseconds
     * @param hat the hat index
     * @param value the hat value (see {@link dev.isxander.sdl3java.api.joystick.SdlJoystickHatConst}
     */
    record JoyHat(
            SDL_JoystickID which,
            long timestamp,
            byte hat,
            @MagicConstant(valuesFromClass = SdlJoystickHatConst.class) byte value
    ) implements JoystickEvent {}

    /**
     * A joystick button event.
     * @param which the joystick ID
     * @param timestamp the event timestamp, in nanoseconds
     * @param button the button index
     * @param down true if the button is pressed, false if released
     */
    record JoyButton(
            SDL_JoystickID which,
            long timestamp,
            byte button,
            boolean down
    ) implements JoystickEvent, ButtonEvent {}

    /**
     * A joystick battery level change event.
     * @param which the joystick ID
     * @param timestamp the event timestamp, in nanoseconds
     * @param state the power state, see {@link SDL_PowerState}
     * @param percent the battery percentage, in the range [0, 100], or -1 if unknown
     */
    record Battery(
            SDL_JoystickID which,
            long timestamp,
            PowerState state,
            int percent
    ) implements SdlControllerEvent {
        public enum PowerState {
            ERROR,
            UNKNOWN,
            ON_BATTERY,
            NO_BATTERY,
            CHARGING,
            CHARGED,
        }
    }

    /**
     * A gamepad device event.
     * @param which the joystick ID
     * @param timestamp the event timestamp, in nanoseconds
     * @param type the type of device event
     */
    record GamepadDevice(
            SDL_JoystickID which,
            long timestamp,
            Type type
    ) implements GamepadEvent, DeviceEvent {}

    /**
     * A gamepad axis motion event.
     * @param which the joystick ID
     * @param timestamp the event timestamp, in nanoseconds
     * @param axis the axis, see {@link SDL_GamepadAxis}
     * @param value the axis value, in the range [-32768, 32767]
     */
    record GamepadAxis(
            SDL_JoystickID which,
            long timestamp,
            @MagicConstant(valuesFromClass = SDL_GamepadAxis.class) byte axis,
            short value
    ) implements GamepadEvent, AxisEvent {}

    /**
     * A gamepad button event.
     * @param which the joystick ID
     * @param timestamp the event timestamp, in nanoseconds
     * @param button the button, see {@link SDL_GamepadButton}
     * @param down true if the button is pressed, false if released
     */
    record GamepadButton(
            SDL_JoystickID which,
            long timestamp,
            @MagicConstant(valuesFromClass = SDL_GamepadButton.class) byte button,
            boolean down
    ) implements GamepadEvent, ButtonEvent {}

    /**
     * A gamepad touchpad event.
     * @param which the joystick ID
     * @param timestamp the event timestamp, in nanoseconds
     * @param touchpad the touchpad index
     * @param finger the finger index
     * @param x the x position, in the range [0.0, 1.0], where 0.0 is the left edge
     * @param y the y position, in the range [0.0, 1.0], where 0.0 is the top edge
     * @param pressure the pressure, in the range [0.0, 1.0], where 0.0 is no pressure and 1.0 is maximum pressure
     */
    record GamepadTouchpad(
            SDL_JoystickID which,
            long timestamp,
            int touchpad,
            int finger,
            float x,
            float y,
            float pressure
    ) implements GamepadEvent {}

    /**
     * A gamepad sensor update event.
     * @param which the joystick ID
     * @param timestamp the event timestamp, in nanoseconds
     * @param sensor the sensor type, see {@link SDL_SensorType}
     * @param data the sensor data, in IMU samples, in rate domain. For example radians per second.
     * @param sensorTimestamp the timestamp reported by the sensor,
     *                        may not be in the same time domain as {@link SdlControllerEvent#timestamp()}/system clock
     */
    record GamepadSensor(
            SDL_JoystickID which,
            long timestamp,
            @MagicConstant(valuesFromClass = SDL_SensorType.class) int sensor,
            float[] data,
            long sensorTimestamp
    ) implements GamepadEvent {}

    class AbstractionStage implements EventStage<SdlEventTypes.SDL_Event, SdlControllerEvent> {
        @Override
        public void onEvent(SdlEventTypes.SDL_Event event, EventSink<? super SdlControllerEvent> downstream) {
            switch (event.type) {
                case SDL_EVENT_JOYSTICK_ADDED,
                     SDL_EVENT_JOYSTICK_REMOVED,
                     SDL_EVENT_JOYSTICK_UPDATE_COMPLETE -> {
                    var d = event.jdevice;
                    downstream.accept(new JoyDevice(
                            d.which,
                            d.timestamp,
                            switch (event.type) {
                                case SDL_EVENT_JOYSTICK_ADDED -> DeviceEvent.Type.ADDED;
                                case SDL_EVENT_JOYSTICK_REMOVED -> DeviceEvent.Type.REMOVED;
                                case SDL_EVENT_JOYSTICK_UPDATE_COMPLETE -> DeviceEvent.Type.UPDATE_COMPLETE;
                                default -> throw new IllegalStateException("Unexpected value: " + event.type);
                            }
                    ));
                }
                case SDL_EVENT_JOYSTICK_BATTERY_UPDATED -> {
                    var b = event.jbattery;
                    downstream.accept(new Battery(
                            b.which,
                            b.timestamp,
                            switch (b.state) {
                                case SDL_POWERSTATE_ERROR -> Battery.PowerState.ERROR;
                                case SDL_POWERSTATE_UNKNOWN ->  Battery.PowerState.UNKNOWN;
                                case SDL_POWERSTATE_ON_BATTERY ->  Battery.PowerState.ON_BATTERY;
                                case SDL_POWERSTATE_NO_BATTERY ->  Battery.PowerState.NO_BATTERY;
                                case SDL_POWERSTATE_CHARGING ->  Battery.PowerState.CHARGING;
                                case SDL_POWERSTATE_CHARGED ->  Battery.PowerState.CHARGED;
                                default -> throw new IllegalStateException("Unexpected value: " + b.state);
                            },
                            b.percent
                    ));
                }
                case SDL_EVENT_JOYSTICK_BUTTON_DOWN, SDL_EVENT_JOYSTICK_BUTTON_UP -> {
                    var b = event.jbutton;
                    downstream.accept(new JoyButton(
                            b.which,
                            b.timestamp,
                            b.button,
                            b.down
                    ));
                }
                case SDL_EVENT_JOYSTICK_AXIS_MOTION -> {
                    var a = event.jaxis;
                    downstream.accept(new JoyAxis(
                            a.which,
                            a.timestamp,
                            a.axis,
                            a.value
                    ));
                }
                case SDL_EVENT_JOYSTICK_HAT_MOTION -> {
                    var h = event.jhat;
                    downstream.accept(new JoyHat(
                            h.which,
                            h.timestamp,
                            h.hat,
                            h.value
                    ));
                }
                case SDL_EVENT_JOYSTICK_BALL_MOTION -> {
                    var b = event.jball;
                    downstream.accept(new JoyBall(
                            b.which,
                            b.timestamp,
                            b.ball,
                            b.xrel,
                            b.yrel
                    ));
                }
                case SDL_EVENT_GAMEPAD_ADDED,
                     SDL_EVENT_GAMEPAD_REMOVED,
                     SDL_EVENT_GAMEPAD_REMAPPED,
                     SDL_EVENT_GAMEPAD_STEAM_HANDLE_UPDATED,
                     SDL_EVENT_GAMEPAD_UPDATE_COMPLETE -> {
                    var d = event.gdevice;
                    downstream.accept(new GamepadDevice(
                            d.which,
                            d.timestamp,
                            switch (event.type) {
                                case SDL_EVENT_GAMEPAD_ADDED -> DeviceEvent.Type.ADDED;
                                case SDL_EVENT_GAMEPAD_REMOVED -> DeviceEvent.Type.REMOVED;
                                case SDL_EVENT_GAMEPAD_REMAPPED -> DeviceEvent.Type.REMAPPED;
                                case SDL_EVENT_GAMEPAD_STEAM_HANDLE_UPDATED -> DeviceEvent.Type.STEAM_HANDLE_UPDATED;
                                case SDL_EVENT_GAMEPAD_UPDATE_COMPLETE -> DeviceEvent.Type.UPDATE_COMPLETE;
                                default -> throw new IllegalStateException("Unexpected value: " + event.type);
                            }
                    ));
                }
                case SDL_EVENT_GAMEPAD_BUTTON_DOWN,
                     SDL_EVENT_GAMEPAD_BUTTON_UP -> {
                    var b = event.gbutton;
                    downstream.accept(new GamepadButton(
                            b.which,
                            b.timestamp,
                            b.button,
                            b.down
                    ));
                }
                case SDL_EVENT_GAMEPAD_AXIS_MOTION -> {
                    var a = event.gaxis;
                    downstream.accept(new GamepadAxis(
                            a.which,
                            a.timestamp,
                            a.axis,
                            a.value
                    ));
                }
                case SDL_EVENT_GAMEPAD_SENSOR_UPDATE -> {
                    var s = event.gsensor;

                    // copy data to avoid retaining reference to the native memory
                    var data = new float[s.data.length];
                    System.arraycopy(s.data, 0, data, 0, s.data.length);

                    downstream.accept(new GamepadSensor(
                            s.which,
                            s.timestamp,
                            s.sensor,
                            data,
                            s.sensor_timestamp
                    ));
                }
            }
        }
    }
}
