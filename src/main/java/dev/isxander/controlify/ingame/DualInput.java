package dev.isxander.controlify.ingame;

import net.minecraft.client.player.Input;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;

public class DualInput extends Input {
    private final Input input1, input2;

    public DualInput(Input input1, Input input2) {
        Validate.isTrue(!(input1 instanceof DualInput), "Cannot nest DualInputs");
        Validate.isTrue(!(input2 instanceof DualInput), "Cannot nest DualInputs");

        this.input1 = input1;
        this.input2 = input2;
    }

    @Override
    public void tick(boolean slowDown, float movementMultiplier) {
        input1.tick(slowDown, movementMultiplier);
        input2.tick(slowDown, movementMultiplier);

        this.left = input1.left || input2.left;
        this.right = input1.right || input2.right;
        this.up = input1.up || input2.up;
        this.down = input1.down || input2.down;
        this.jumping = input1.jumping || input2.jumping;
        this.shiftKeyDown = input1.shiftKeyDown || input2.shiftKeyDown;
        this.leftImpulse = Mth.clamp(input1.leftImpulse + input2.leftImpulse, -1, 1);
        this.forwardImpulse = Mth.clamp(input1.forwardImpulse + input2.forwardImpulse, -1, 1);
    }
}
