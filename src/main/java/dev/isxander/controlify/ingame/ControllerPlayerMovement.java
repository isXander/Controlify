package dev.isxander.controlify.ingame;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;

public class ControllerPlayerMovement extends ClientInput {
    private final ControllerEntity controller;
    private final LocalPlayer player;
    private boolean wasFlying, wasPassenger;

    public ControllerPlayerMovement(ControllerEntity controller, LocalPlayer player) {
        this.controller = controller;
        this.player = player;
    }

    @Override
    public void tick() {
        if (Minecraft.getInstance().screen != null || player == null) {
            this.setMoveVec(0, 0);

            this.keyPresses = Input.EMPTY;

            return;
        }

        float forwardImpulse = ControlifyBindings.WALK_FORWARD.on(controller).analogueNow()
                - ControlifyBindings.WALK_BACKWARD.on(controller).analogueNow();
        float leftImpulse = ControlifyBindings.WALK_LEFT.on(controller).analogueNow()
                - ControlifyBindings.WALK_RIGHT.on(controller).analogueNow();

        if (Controlify.instance().config().getSettings().globalSettings().shouldUseKeyboardMovement()) {
            float threshold = controller.input().orElseThrow().settings().buttonActivationThreshold;

            forwardImpulse = Math.abs(forwardImpulse) >= threshold ? Math.copySign(1, forwardImpulse) : 0;
            leftImpulse = Math.abs(leftImpulse) >= threshold ? Math.copySign(1, leftImpulse) : 0;
        }

        boolean shiftKeyDown = keyPresses.shift();
        boolean jumping = keyPresses.jump();

        boolean up = forwardImpulse > 0;
        boolean down = forwardImpulse < 0;
        boolean left = leftImpulse > 0;
        boolean right = leftImpulse < 0;

        this.setMoveVec(forwardImpulse, leftImpulse);

        // this over-complication is so exiting a GUI with the button still held doesn't trigger a jump.
        InputBinding jump = ControlifyBindings.JUMP.on(controller);
        if (jump.justPressed())
            jumping = true;
        if (!jump.digitalNow())
            jumping = false;

        InputBinding sneak = ControlifyBindings.SNEAK.on(controller);
        if (player.getAbilities().flying || (player.isInWater() && !player.onGround()) || player.getVehicle() != null || !controller.settings().generic.toggleSneak) {
            if (sneak.justPressed())
                shiftKeyDown = true;
            if (!sneak.digitalNow())
                shiftKeyDown = false;
        } else {
            if (sneak.justPressed()) {
                shiftKeyDown = !shiftKeyDown;
            }
        }
        if ((!player.getAbilities().flying && wasFlying && player.onGround()) || (!player.isPassenger() && wasPassenger)) {
            shiftKeyDown = false;
        }

        boolean sprinting = ControlifyBindings.SPRINT.on(controller).digitalNow();

        this.keyPresses = new Input(up, down, left, right, jumping, shiftKeyDown, sprinting);

        this.wasFlying = player.getAbilities().flying;
        this.wasPassenger = player.isPassenger();
    }

    private void setMoveVec(float forward, float left) {
        /*
        Starting 25w02a, movement vector is normalised (length set to 1). This won't work in analogue input, as
        it would mean you wouldn't be able to move any slower than full speed. So instead, Controlify *limits* the
        vector length to 1, but doesn't normalise it.
        With regular thumb-sticks, circularity is already a thing,
        so this won't actually make any difference for most people.
        But custom joystick configurations may produce irregular results, hence this is necessary.
         */
        this.moveVector = new Vec2(left, forward);
        float length = this.moveVector.length();
        if (length > 1) {
            this.moveVector = this.moveVector.scale(1f / length);
        }
    }

    public static void updatePlayerInput(@Nullable LocalPlayer player) {
        if (player == null)
            return;

        if (shouldBeControllerInput()) {
            player.input = new DualInput(
                    new KeyboardInput(Minecraft.getInstance().options),
                    new ControllerPlayerMovement(Controlify.instance().getCurrentController().get(), player)
            );
        } else if (!(player.input instanceof KeyboardInput)) {
            player.input = new KeyboardInput(Minecraft.getInstance().options);
        }
    }

    public static void ensureCorrectInput(@Nullable LocalPlayer player) {
        if (player == null)
            return;

        if (shouldBeControllerInput() && player.input.getClass() == KeyboardInput.class) {
            updatePlayerInput(player);
        }
    }

    public static boolean shouldBeControllerInput() {
        return Controlify.instance().getCurrentController().isPresent()
                && Controlify.instance().currentInputMode().isController();
    }
}
