package dev.isxander.controlify.ingame;

import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;

//? if >=1.21.2 {
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
//?} else {
/*import net.minecraft.client.player.Input;
*///?}

public class DualInput extends /*? if >=1.21.2 {*/ ClientInput /*?} else {*/ /*Input *//*?}*/ {
    //? if >=1.21.2 {
    private final ClientInput input1, input2;
    //?} else {
    /*private final Input input1, input2;
    *///?}

    public DualInput(
            //? if >=1.21.2 {
            ClientInput input1, ClientInput input2
            //?} else {
            /*Input input1, Input input2
            *///?}
    ) {
        Validate.isTrue(!(input1 instanceof DualInput), "Cannot nest DualInputs");
        Validate.isTrue(!(input2 instanceof DualInput), "Cannot nest DualInputs");

        this.input1 = input1;
        this.input2 = input2;
    }

    @Override
    public void tick(boolean slowDown, float movementMultiplier) {
        input1.tick(slowDown, movementMultiplier);
        input2.tick(slowDown, movementMultiplier);

        this.leftImpulse = Mth.clamp(input1.leftImpulse + input2.leftImpulse, -1, 1);
        this.forwardImpulse = Mth.clamp(input1.forwardImpulse + input2.forwardImpulse, -1, 1);

        //? if >=1.21.2 {
        Input input1 = this.input1.keyPresses;
        Input input2 = this.input2.keyPresses;
        this.keyPresses = new Input(
                input1.forward() || input2.forward(),
                input1.backward() || input2.backward(),
                input1.left() || input2.left(),
                input1.right() || input2.right(),
                input1.jump() || input2.jump(),
                input1.shift() || input2.shift(),
                input1.sprint() || input2.sprint()
        );
        //?} else {
        /*this.left = input1.left || input2.left;
        this.right = input1.right || input2.right;
        this.up = input1.up || input2.up;
        this.down = input1.down || input2.down;
        this.jumping = input1.jumping || input2.jumping;
        this.shiftKeyDown = input1.shiftKeyDown || input2.shiftKeyDown;
        *///?}
    }
}
