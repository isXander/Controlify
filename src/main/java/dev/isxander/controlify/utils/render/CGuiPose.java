package dev.isxander.controlify.utils.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;

public interface CGuiPose {
    CGuiPose push();

    CGuiPose pop();

    CGuiPose translate(float x, float y);

    CGuiPose scale(float x, float y);

    CGuiPose nextLayer(float legacyZShift);

    static CGuiPose of(GuiGraphics graphics) {
        //? if >=1.21.6 {
        return new Impl2D(graphics);
        //?} else {
        /*return new Impl3D(graphics.pose());
        *///?}
    }

    static CGuiPose ofPush(GuiGraphics graphics) {
        return of(graphics).push();
    }

    //? if >=1.21.6 {
    class Impl2D implements CGuiPose {
        private final GuiGraphics graphics;

        public Impl2D(GuiGraphics graphics) {
            this.graphics = graphics;
        }

        private Matrix3x2fStack stack() {
            return this.graphics.pose();
        }

        @Override
        public CGuiPose push() {
            this.stack().pushMatrix();
            return this;
        }

        @Override
        public CGuiPose pop() {
            this.stack().popMatrix();
            return this;
        }

        @Override
        public CGuiPose translate(float x, float y) {
            this.stack().translate(x, y);
            return this;
        }

        @Override
        public CGuiPose scale(float x, float y) {
            this.stack().scale(x, y);
            return this;
        }

        @Override
        public CGuiPose nextLayer(float legacyZShift) {
            this.graphics.nextStratum();
            return this;
        }
    }
    //?} else {
    /*class Impl3D implements CGuiPose {
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

        @Override
        public CGuiPose nextLayer(float legacyZShift) {
            this.stack.translate(0, 0, legacyZShift);
            return this;
        }
    }
    *///?}
}
