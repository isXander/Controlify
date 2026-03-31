package dev.isxander.controlify.ingame;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.lang3.Validate;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;

public class DualInput extends ClientInput {
    private final ClientInput input1, input2;

    public DualInput(ClientInput input1, ClientInput input2) {
        Validate.isTrue(!(input1 instanceof DualInput), "Cannot nest DualInputs");
        Validate.isTrue(!(input2 instanceof DualInput), "Cannot nest DualInputs");

        this.input1 = input1;
        this.input2 = input2;
    }

    @Override
    public void tick() {
        input1.tick();
        input2.tick();

        Vec2 input1MoveVec = InGameInputHandler.getMoveVec(input1);
        Vec2 input2MoveVec = InGameInputHandler.getMoveVec(input2);
        this.setMoveVec(
                Mth.clamp(input1MoveVec.y + input2MoveVec.y, -1, 1),
                Mth.clamp(input1MoveVec.x + input2MoveVec.x, -1, 1)
        );

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
    }

    private void setMoveVec(float forward, float left) {
        this.moveVector = new Vec2(left, forward);
    }
}
