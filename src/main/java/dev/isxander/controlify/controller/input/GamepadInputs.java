package dev.isxander.controlify.controller.input;

import com.google.common.collect.Sets;
import dev.isxander.controlify.bindings.input.AxisInput;
import dev.isxander.controlify.bindings.input.ButtonInput;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

public final class GamepadInputs {
    private GamepadInputs() {
    }

    public static final ResourceLocation SOUTH_BUTTON = button("south");
    public static final ResourceLocation EAST_BUTTON = button("east");
    public static final ResourceLocation WEST_BUTTON = button("west");
    public static final ResourceLocation NORTH_BUTTON = button("north");

    public static final ResourceLocation LEFT_SHOULDER_BUTTON = button("left_shoulder");
    public static final ResourceLocation RIGHT_SHOULDER_BUTTON = button("right_shoulder");

    public static final ResourceLocation LEFT_STICK_BUTTON = button("left_stick");
    public static final ResourceLocation RIGHT_STICK_BUTTON = button("right_stick");

    public static final ResourceLocation BACK_BUTTON = button("back");
    public static final ResourceLocation START_BUTTON = button("start");
    public static final ResourceLocation GUIDE_BUTTON = button("guide");

    public static final ResourceLocation DPAD_UP_BUTTON = button("dpad_up");
    public static final ResourceLocation DPAD_DOWN_BUTTON = button("dpad_down");
    public static final ResourceLocation DPAD_LEFT_BUTTON = button("dpad_left");
    public static final ResourceLocation DPAD_RIGHT_BUTTON = button("dpad_right");

    public static final ResourceLocation LEFT_TRIGGER_AXIS = axis("left_trigger");
    public static final ResourceLocation RIGHT_TRIGGER_AXIS = axis("right_trigger");

    public static final ResourceLocation LEFT_STICK_AXIS_UP = axis("left_stick_up");
    public static final ResourceLocation LEFT_STICK_AXIS_DOWN = axis("left_stick_down");
    public static final ResourceLocation LEFT_STICK_AXIS_LEFT = axis("left_stick_left");
    public static final ResourceLocation LEFT_STICK_AXIS_RIGHT = axis("left_stick_right");

    public static final ResourceLocation RIGHT_STICK_AXIS_UP = axis("right_stick_up");
    public static final ResourceLocation RIGHT_STICK_AXIS_DOWN = axis("right_stick_down");
    public static final ResourceLocation RIGHT_STICK_AXIS_LEFT = axis("right_stick_left");
    public static final ResourceLocation RIGHT_STICK_AXIS_RIGHT = axis("right_stick_right");

    // ADDITIONAL INPUTS - NOT PRESENT ON ALL GAMEPADS
    public static final ResourceLocation MISC_1_BUTTON = button("misc_1");
    public static final ResourceLocation MISC_2_BUTTON = button("misc_2");
    public static final ResourceLocation MISC_3_BUTTON = button("misc_3");
    public static final ResourceLocation MISC_4_BUTTON = button("misc_4");
    public static final ResourceLocation MISC_5_BUTTON = button("misc_5");
    public static final ResourceLocation MISC_6_BUTTON = button("misc_6");


    public static final ResourceLocation RIGHT_PADDLE_1_BUTTON = button("right_paddle_1");
    public static final ResourceLocation RIGHT_PADDLE_2_BUTTON = button("right_paddle_2");
    public static final ResourceLocation LEFT_PADDLE_1_BUTTON = button("left_paddle_1");
    public static final ResourceLocation LEFT_PADDLE_2_BUTTON = button("left_paddle_2");

    public static final ResourceLocation TOUCHPAD_BUTTON = button("touchpad");

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

    public static Input getBind(ResourceLocation id) {
        return switch (id.getPath().split("/")[0]) {
            case "button" -> new ButtonInput(id);
            case "axis" -> new AxisInput(id);
            case "hat" -> throw new IllegalArgumentException("Gamepad does not have hat inputs.");
            default -> throw new IllegalArgumentException("Unknown bind type: " + id);
        };
    }

    private static ResourceLocation button(String id) {
        return CUtil.rl("button/" + id);
    }
    private static ResourceLocation axis(String id) {
        return CUtil.rl("axis/" + id);
    }
    private static ResourceLocation hat(String id) {
        return CUtil.rl("hat/" + id);
    }
}
