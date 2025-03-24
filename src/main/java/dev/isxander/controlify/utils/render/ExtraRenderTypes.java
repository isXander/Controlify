package dev.isxander.controlify.utils.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class ExtraRenderTypes /*? if <1.21.2 {*/ /*extends RenderType *//*?}*/ {
    //? if <1.21.2 {
    /*private static final Function<ResourceLocation, RenderType> GUI_TEXTURED = Util.memoize(
            resourceLocation -> RenderType.create(
                    "controlify:gui_textured",
                    DefaultVertexFormat.POSITION_TEX_COLOR,
                    VertexFormat.Mode.QUADS,
                    786432,
                    false, false,
                    RenderType.CompositeState.builder()
                            .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                            .setShaderState(RenderStateShard.RENDERTYPE_GUI_SHADER)
                            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                            .createCompositeState(false)
            )
    );

    public ExtraRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
    *///?}

    public static RenderType guiTextured(ResourceLocation texture) {
        //? if >=1.21.2 {
        return RenderType.guiTextured(texture);
        //?} else {
        /*return GUI_TEXTURED.apply(texture);
        *///?}
    }
}
