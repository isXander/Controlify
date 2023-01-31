package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.Controller;

public class ControllerBindings {
    public final ControllerBinding JUMP, SNEAK, ATTACK, USE, SPRINT, NEXT_SLOT, PREV_SLOT;
    public final ControllerBinding[] ALL;

    public ControllerBindings(Controller controller) {
        JUMP = new ControllerBinding(controller, Bind.A_BUTTON, "jump");
        SNEAK = new ControllerBinding(controller, Bind.RIGHT_STICK, "sneak");
        ATTACK = new ControllerBinding(controller, Bind.RIGHT_TRIGGER, "attack");
        USE = new ControllerBinding(controller, Bind.LEFT_TRIGGER, "use");
        SPRINT = new ControllerBinding(controller, Bind.LEFT_STICK, "sprint");
        NEXT_SLOT = new ControllerBinding(controller, Bind.RIGHT_BUMPER, "next_slot");
        PREV_SLOT = new ControllerBinding(controller, Bind.LEFT_BUMPER, "prev_slot");

        ALL = new ControllerBinding[] {
                JUMP, SNEAK, ATTACK, USE, SPRINT, NEXT_SLOT, PREV_SLOT
        };
    }
}
