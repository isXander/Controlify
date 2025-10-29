package dev.isxander.controlify.ingame;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.input.action.ActionHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
//? if >=1.21.2 {
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
//?} else {
/*import net.minecraft.client.player.Input;
*///?}

public class ControllerPlayerMovement extends /*? if >=1.21.2 {*/ ClientInput /*?} else {*/ /*Input *//*?}*/ {
    private final ControllerEntity controller;
    private final LocalPlayer player;
    private boolean wasFlying, wasPassenger;

    public ControllerPlayerMovement(ControllerEntity controller, LocalPlayer player) {
        this.controller = controller;
        this.player = player;
    }

    @Override
    //? if >=1.21.4 {
    public void tick() {
    //?} else {
    /*public void tick(boolean slowDown, float movementMultiplier) {
    *///?}
        if (Minecraft.getInstance().screen != null || player == null) {
            this.setMoveVec(0, 0);

            //? if >=1.21.2 {
            this.keyPresses = Input.EMPTY;
            //?} else {
            /*this.up = false;
            this.down = false;
            this.left = false;
            this.right = false;
            this.jumping = false;
            this.shiftKeyDown = false;
            *///?}

            return;
        }

        float forwardImpulse = ControlifyBindings.WALK_FORWARD.on(controller).getContinuous()
                - ControlifyBindings.WALK_BACKWARD.on(controller).getContinuous();
        float leftImpulse = ControlifyBindings.WALK_LEFT.on(controller).getContinuous()
                - ControlifyBindings.WALK_RIGHT.on(controller).getContinuous();

        if (Controlify.instance().config().globalSettings().shouldUseKeyboardMovement()) {
            float threshold = controller.input().orElseThrow().confObj().buttonActivationThreshold;

            forwardImpulse = Math.abs(forwardImpulse) >= threshold ? Math.copySign(1, forwardImpulse) : 0;
            leftImpulse = Math.abs(leftImpulse) >= threshold ? Math.copySign(1, leftImpulse) : 0;
        }

        //? if >=1.21.2 {
        boolean up, down, left, right;
        boolean shiftKeyDown = keyPresses.shift();
        boolean jumping = keyPresses.jump();
        //?}

        up = forwardImpulse > 0;
        down = forwardImpulse < 0;
        left = leftImpulse > 0;
        right = leftImpulse < 0;

        //? if >=1.21.4 {
        //?} else {
        /*if (slowDown) {
            leftImpulse *= movementMultiplier;
            forwardImpulse *= movementMultiplier;
        }
        *///?}
        this.setMoveVec(forwardImpulse, leftImpulse);

        jumping = ControlifyBindings.JUMP.on(controller).isLatchActive();

        boolean sneakActionLatch = ControlifyBindings.SNEAK.on(controller).isLatchActive();
        boolean toggleSneak = controller.genericConfig().config().toggleSneak;
        boolean canSneak = player.getAbilities().flying || (player.isInWater() && !player.onGround()) || player.getVehicle() != null;
        if (canSneak || !toggleSneak) {
            shiftKeyDown = sneakActionLatch;
        } else {
            if (sneakActionLatch) {  // TODO: this won't work - need to check for just pressed on a latch which doesn't make sense
                shiftKeyDown = !shiftKeyDown;
            }
        }
        if ((!player.getAbilities().flying && wasFlying && player.onGround()) || (!player.isPassenger() && wasPassenger)) {
            shiftKeyDown = false;
        }

        //? if >=1.21.2 {
        boolean sprinting = ControlifyBindings.SPRINT.on(controller).digitalNow();

        this.keyPresses = new Input(up, down, left, right, jumping, shiftKeyDown, sprinting);
        //?}

        this.wasFlying = player.getAbilities().flying;
        this.wasPassenger = player.isPassenger();
    }

    private void setMoveVec(float forward, float left) {
        //? if >=1.21.5 {
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

        //?} else {
        /*this.forwardImpulse = forward;
        this.leftImpulse = left;
        *///?}
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
        return Controlify.instance().getCurrentController().isPresent() && Controlify.instance().currentInputMode().isController();
    }
}
