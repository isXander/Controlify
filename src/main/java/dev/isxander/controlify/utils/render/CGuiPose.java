package dev.isxander.controlify.utils.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;

public interface CGuiPose {
    CGuiPose push();

    CGuiPose pop();

    CGuiPose translate(float x, float y);

    CGuiPose scale(float x, float y);

    static CGuiPose of(GuiGraphics graphics) {
        //? if >=1.21.6 {
        /*return new Impl2D(graphics.pose());
        *///?} else {
        return new Impl3D(graphics.pose());
        //?}
    }

    static CGuiPose ofPush(GuiGraphics graphics) {
        return of(graphics).push();
    }

    class Impl2D implements CGuiPose {
        private final Matrix3x2fStack stack;

        public Impl2D(Matrix3x2fStack stack) {
            this.stack = stack;
        }

        @Override
        public CGuiPose push() {
            this.stack.pushMatrix();
            return this;
        }

        @Override
        public CGuiPose pop() {
            this.stack.popMatrix();
            return this;
        }

        @Override
        public CGuiPose translate(float x, float y) {
            this.stack.translate(x, y);
            return this;
        }

        @Override
        public CGuiPose scale(float x, float y) {
            this.stack.scale(x, y);
            return this;
        }
    }

    class Impl3D implements CGuiPose {
        private final PoseStack stack;

        public Impl3D(PoseStack stack) {
            this.stack = stack;
        }

        @Override
        public CGuiPose push() {
            this.stack.pushPose();
            return this;
        }

        @Override
        public CGuiPose pop() {
            this.stack.popPose();
            return this;
        }

        @Override
        public CGuiPose translate(float x, float y) {
            this.stack.translate(x, y, 0);
            return this;
        }

        @Override
        public CGuiPose scale(float x, float y) {
            this.stack.scale(x, y, 1);
            return this;
        }
    }
}
