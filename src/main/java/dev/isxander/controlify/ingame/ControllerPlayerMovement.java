package dev.isxander.controlify.ingame;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;

public class ControllerPlayerMovement extends Input {
    private final Controller<?, ?> controller;
    private final LocalPlayer player;

    public ControllerPlayerMovement(Controller<?, ?> controller, LocalPlayer player) {
        this.controller = controller;
        this.player = player;
    }

    @Override
    public void tick(boolean slowDown, float f) {
        if (Minecraft.getInstance().screen != null || player == null) {
            this.up = false;
            this.down = false;
            this.left = false;
            this.right = false;
            this.leftImpulse = 0;
            this.forwardImpulse = 0;
            this.jumping = false;
            this.shiftKeyDown = false;
            return;
        }

        var bindings = controller.bindings();

        this.forwardImpulse = bindings.WALK.value().vector().y();
        this.leftImpulse = bindings.WALK.value().vector().x();

        // .1 to prevent using boat turning absolute hell with left/right left/right
        this.up = forwardImpulse > 0.1;
        this.down = forwardImpulse < -0.1;
        this.left = leftImpulse > 0.1;
        this.right = leftImpulse < -0.1;

        if (Controlify.instance().config().globalSettings().keyboardMovement) {
            this.forwardImpulse = Math.signum(this.forwardImpulse);
            this.leftImpulse = Math.signum(this.leftImpulse);
        }

        if (slowDown) {
            this.leftImpulse *= f;
            this.forwardImpulse *= f;
        }

        if (!this.jumping && bindings.JUMP.justPressed())
            this.jumping = true;
        else
            this.jumping = bindings.JUMP.held();

        if (player.getAbilities().flying || player.isInWater() || !controller.config().toggleSneak) {
            this.shiftKeyDown = bindings.SNEAK.held();
        } else {
            this.shiftKeyDown = Minecraft.getInstance().options.keyShift.isDown();
        }
    }
}
