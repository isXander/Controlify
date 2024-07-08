package dev.isxander.controlify.utils.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;

public interface ControlifyVertexConsumer {
    ControlifyVertexConsumer vertex(float x, float y, float z);

    ControlifyVertexConsumer vertex(Matrix4f matrix, float x, float y, float z);

    ControlifyVertexConsumer endVertex();

    ControlifyVertexConsumer color(int red, int green, int blue, int alpha);

    default ControlifyVertexConsumer color(int color) {
        return this.color(
                FastColor.ARGB32.red(color),
                FastColor.ARGB32.green(color),
                FastColor.ARGB32.blue(color),
                FastColor.ARGB32.alpha(color)
        );
    }

    ControlifyVertexConsumer uv(float u, float v);

    ControlifyVertexConsumer uv2(int u, int v);

    ControlifyVertexConsumer normal(float x, float y, float z);

    VertexConsumer getVanilla();

    static ControlifyVertexConsumer of(VertexConsumer vanilla) {
        //? if >=1.21 {
        return new Post21VertexConsumer(vanilla);
        //?} else {
        /*return new Pre21VertexConsumer(vanilla);
        *///?}
    }

    //? if >=1.21 {
    class Post21VertexConsumer implements ControlifyVertexConsumer {
        private final VertexConsumer vertexConsumer;

        public Post21VertexConsumer(VertexConsumer vertexConsumer) {
            this.vertexConsumer = vertexConsumer;
        }

        @Override
        public ControlifyVertexConsumer vertex(float x, float y, float z) {
            vertexConsumer.addVertex(x, y, z);
            return this;
        }

        @Override
        public ControlifyVertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
            vertexConsumer.addVertex(matrix, x, y, z);
            return this;
        }

        @Override
        public ControlifyVertexConsumer endVertex() {
            return this;
        }

        @Override
        public ControlifyVertexConsumer color(int red, int green, int blue, int alpha) {
            vertexConsumer.setColor(red, green, blue, alpha);
            return this;
        }

        @Override
        public ControlifyVertexConsumer uv(float u, float v) {
            vertexConsumer.setUv(u, v);
            return this;
        }

        @Override
        public ControlifyVertexConsumer uv2(int u, int v) {
            vertexConsumer.setUv2(u, v);
            return this;
        }

        @Override
        public ControlifyVertexConsumer normal(float x, float y, float z) {
            vertexConsumer.setNormal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer getVanilla() {
            return vertexConsumer;
        }
    }
    //?} else {
    /*class Pre21VertexConsumer implements ControlifyVertexConsumer {
        private final VertexConsumer vertexConsumer;

        public Pre21VertexConsumer(VertexConsumer vertexConsumer) {
            this.vertexConsumer = vertexConsumer;
        }

        @Override
        public ControlifyVertexConsumer vertex(float x, float y, float z) {
            vertexConsumer.vertex(x, y, z);
            return this;
        }

        @Override
        public ControlifyVertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
            vertexConsumer.vertex(matrix, x, y, z);
            return this;
        }

        @Override
        public ControlifyVertexConsumer endVertex() {
            vertexConsumer.endVertex();
            return this;
        }

        @Override
        public ControlifyVertexConsumer color(int red, int green, int blue, int alpha) {
            vertexConsumer.color(red, green, blue, alpha);
            return this;
        }

        @Override
        public ControlifyVertexConsumer uv(float u, float v) {
            vertexConsumer.uv(u, v);
            return this;
        }

        @Override
        public ControlifyVertexConsumer uv2(int u, int v) {
            vertexConsumer.uv2(u, v);
            return this;
        }

        @Override
        public ControlifyVertexConsumer normal(float x, float y, float z) {
            vertexConsumer.normal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer getVanilla() {
            return vertexConsumer;
        }
    }
    *///?}
}
