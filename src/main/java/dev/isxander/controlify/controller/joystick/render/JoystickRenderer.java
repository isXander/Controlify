package dev.isxander.controlify.controller.joystick.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.bindings.JoystickAxisBind;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.gui.DrawSize;

public interface JoystickRenderer {
    int DEFAULT_SIZE = 22;

    DrawSize render(PoseStack poseStack, int x, int centerY, int size);

    interface Button extends JoystickRenderer {
        DrawSize render(PoseStack poseStack, int x, int centerY, int size, boolean down);

        default DrawSize render(PoseStack poseStack, int x, int centerY, boolean down) {
            return render(poseStack, x, centerY, DEFAULT_SIZE, down);
        }

        default DrawSize render(PoseStack poseStack, int x, int centerY, int size) {
            return render(poseStack, x, centerY, size, false);
        }
    }

    interface Axis extends JoystickRenderer {
        DrawSize render(PoseStack poseStack, int x, int centerY, int size, JoystickAxisBind.AxisDirection direction);

        default DrawSize render(PoseStack poseStack, int x, int centerY, JoystickAxisBind.AxisDirection direction) {
            return render(poseStack, x, centerY, DEFAULT_SIZE, direction);
        }

        default DrawSize render(PoseStack poseStack, int x, int centerY, int size) {
            return render(poseStack, x, centerY, size, JoystickAxisBind.AxisDirection.POSITIVE);
        }
    }

    interface Hat extends JoystickRenderer {
        DrawSize render(PoseStack poseStack, int x, int centerY, int size, JoystickState.HatState state);

        default DrawSize render(PoseStack poseStack, int x, int centerY, JoystickState.HatState state) {
            return render(poseStack, x, centerY, DEFAULT_SIZE, state);
        }

        default DrawSize render(PoseStack poseStack, int x, int centerY, int size) {
            return render(poseStack, x, centerY, size, JoystickState.HatState.CENTERED);
        }
    }
}
