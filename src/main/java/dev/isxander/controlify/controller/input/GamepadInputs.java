package dev.isxander.controlify.controller.input;

import com.google.common.collect.Sets;
import dev.isxander.controlify.bindings.input.AxisInput;
import dev.isxander.controlify.bindings.input.ButtonInput;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Set;

public final class GamepadInputs {
    private GamepadInputs() {
    }

    public static final Identifier SOUTH_BUTTON = button("south");
    public static final Identifier EAST_BUTTON = button("east");
    public static final Identifier WEST_BUTTON = button("west");
    public static final Identifier NORTH_BUTTON = button("north");

    public static final Identifier LEFT_SHOULDER_BUTTON = button("left_shoulder");
    public static final Identifier RIGHT_SHOULDER_BUTTON = button("right_shoulder");

    public static final Identifier LEFT_STICK_BUTTON = button("left_stick");
    public static final Identifier RIGHT_STICK_BUTTON = button("right_stick");

    public static final Identifier BACK_BUTTON = button("back");
    public static final Identifier START_BUTTON = button("start");
    public static final Identifier GUIDE_BUTTON = button("guide");

    public static final Identifier DPAD_UP_BUTTON = button("dpad_up");
    public static final Identifier DPAD_DOWN_BUTTON = button("dpad_down");
    public static final Identifier DPAD_LEFT_BUTTON = button("dpad_left");
    public static final Identifier DPAD_RIGHT_BUTTON = button("dpad_right");

    public static final Identifier LEFT_TRIGGER_AXIS = axis("left_trigger");
    public static final Identifier RIGHT_TRIGGER_AXIS = axis("right_trigger");

    public static final Identifier LEFT_STICK_AXIS_UP = axis("left_stick_up");
    public static final Identifier LEFT_STICK_AXIS_DOWN = axis("left_stick_down");
    public static final Identifier LEFT_STICK_AXIS_LEFT = axis("left_stick_left");
    public static final Identifier LEFT_STICK_AXIS_RIGHT = axis("left_stick_right");

    public static final Identifier RIGHT_STICK_AXIS_UP = axis("right_stick_up");
    public static final Identifier RIGHT_STICK_AXIS_DOWN = axis("right_stick_down");
    public static final Identifier RIGHT_STICK_AXIS_LEFT = axis("right_stick_left");
    public static final Identifier RIGHT_STICK_AXIS_RIGHT = axis("right_stick_right");

    // ADDITIONAL INPUTS - NOT PRESENT ON ALL GAMEPADS
    public static final Identifier MISC_1_BUTTON = button("misc_1");
    public static final Identifier MISC_2_BUTTON = button("misc_2");
    public static final Identifier MISC_3_BUTTON = button("misc_3");
    public static final Identifier MISC_4_BUTTON = button("misc_4");
    public static final Identifier MISC_5_BUTTON = button("misc_5");
    public static final Identifier MISC_6_BUTTON = button("misc_6");


    public static final Identifier RIGHT_PADDLE_1_BUTTON = button("right_paddle_1");
    public static final Identifier RIGHT_PADDLE_2_BUTTON = button("right_paddle_2");
    public static final Identifier LEFT_PADDLE_1_BUTTON = button("left_paddle_1");
    public static final Identifier LEFT_PADDLE_2_BUTTON = button("left_paddle_2");

    public static final Identifier TOUCHPAD_1_BUTTON = button("touchpad_1");
    public static final Identifier TOUCHPAD_2_BUTTON = button("touchpad_2");

    public static final Set<DeadzoneGroup> DEADZONE_GROUPS = Sets.newLinkedHashSet(List.of(
            new DeadzoneGroup(CUtil.rl("left_stick"), List.of(
                    LEFT_STICK_AXIS_UP,
                    LEFT_STICK_AXIS_DOWN,
                    LEFT_STICK_AXIS_LEFT,
                    LEFT_STICK_AXIS_RIGHT
            )),
            new DeadzoneGroup(CUtil.rl("right_stick"), List.of(
                    RIGHT_STICK_AXIS_UP,
                    RIGHT_STICK_AXIS_DOWN,
                    RIGHT_STICK_AXIS_LEFT,
                    RIGHT_STICK_AXIS_RIGHT
            ))
    ));

    public static Input getBind(Identifier id) {
        return switch (id.getPath().split("/")[0]) {
            case "button" -> new ButtonInput(id);
            case "axis" -> new AxisInput(id);
            case "hat" -> throw new IllegalArgumentException("Gamepad does not have hat inputs.");
            default -> throw new IllegalArgumentException("Unknown bind type: " + id);
        };
    }

    private static Identifier button(String id) {
        return CUtil.rl("button/" + id);
    }
    private static Identifier axis(String id) {
        return CUtil.rl("axis/" + id);
    }
    private static Identifier hat(String id) {
        return CUtil.rl("hat/" + id);
    }
}
