package dev.isxander.controlify.ingame;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
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
    public void tick(boolean slowDown, float movementMultiplier) {
        if (Minecraft.getInstance().screen != null || player == null) {
            this.leftImpulse = 0;
            this.forwardImpulse = 0;

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

        this.forwardImpulse = ControlifyBindings.WALK_FORWARD.on(controller).analogueNow()
                - ControlifyBindings.WALK_BACKWARD.on(controller).analogueNow();
        this.leftImpulse = ControlifyBindings.WALK_LEFT.on(controller).analogueNow()
                - ControlifyBindings.WALK_RIGHT.on(controller).analogueNow();

        if (Controlify.instance().config().globalSettings().shouldUseKeyboardMovement()) {
            float threshold = controller.input().orElseThrow().confObj().buttonActivationThreshold;

            this.forwardImpulse = Math.abs(this.forwardImpulse) >= threshold ? Math.copySign(1, this.forwardImpulse) : 0;
            this.leftImpulse = Math.abs(this.leftImpulse) >= threshold ? Math.copySign(1, this.leftImpulse) : 0;
        }

        //? if >=1.21.2 {
        boolean up, down, left, right;
        boolean shiftKeyDown = keyPresses.shift();
        boolean jumping = keyPresses.jump();
        //?}

        up = this.forwardImpulse > 0;
        down = this.forwardImpulse < 0;
        left = this.leftImpulse > 0;
        right = this.leftImpulse < 0;

        if (slowDown) {
            this.leftImpulse *= movementMultiplier;
            this.forwardImpulse *= movementMultiplier;
        }

        // this over-complication is so exiting a GUI with the button still held doesn't trigger a jump.
        InputBinding jump = ControlifyBindings.JUMP.on(controller);
        if (jump.justPressed())
            jumping = true;
        if (!jump.digitalNow())
            jumping = false;

        InputBinding sneak = ControlifyBindings.SNEAK.on(controller);
        if (player.getAbilities().flying || (player.isInWater() && !player.onGround()) || player.getVehicle() != null || !controller.genericConfig().config().toggleSneak) {
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

        //? if >=1.21.2 {
        boolean sprinting = ControlifyBindings.SPRINT.on(controller).digitalNow();

        this.keyPresses = new Input(up, down, left, right, jumping, shiftKeyDown, sprinting);
        //?}

        this.wasFlying = player.getAbilities().flying;
        this.wasPassenger = player.isPassenger();
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
