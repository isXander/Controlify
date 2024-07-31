package dev.isxander.controlify.utils.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class ExtraRenderTypes extends RenderType {
    public static Function<ResourceLocation, RenderType> BLIT_TEXTURE = Util.memoize(atlas ->
            RenderType.create(
                    "controlify$blit_texture$" + atlas,
                    DefaultVertexFormat.POSITION_TEX,
                    VertexFormat.Mode.QUADS,
                    256,
                    false, false,
                    RenderType.CompositeState.builder()
                            .setShaderState(POSITION_TEX_SHADER)
                            .setTextureState(new TextureStateShard(atlas, false, false))
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .createCompositeState(false)
            ));

    private ExtraRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
}
