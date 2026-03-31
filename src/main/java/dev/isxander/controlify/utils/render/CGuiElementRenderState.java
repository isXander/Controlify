package dev.isxander.controlify.utils.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CGuiElementRenderState extends GuiElementRenderState {

    BaseRenderState baseState();

    @Override
    default @NotNull RenderPipeline pipeline() {
        return baseState().pipeline();
    }

    @Override
    default @NotNull TextureSetup textureSetup() {
        return baseState().textureSetup();
    }

    @Override
    default @Nullable ScreenRectangle scissorArea() {
        return baseState().scissorArea();
    }

    @Override
    default @Nullable ScreenRectangle bounds() {
        return baseState().bounds();
    }

    default void submit(GuiGraphicsExtractor graphics) {
        //? if fabric
        graphics.guiRenderState.addGuiElement(this);
        //? if neoforge
        //graphics.submitGuiElementRenderState(this);
    }
}

