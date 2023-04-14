package dev.isxander.controlify.mixins.feature.guide.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.bind.BindRenderer;
import dev.isxander.controlify.api.buttonguide.ButtonRenderPosition;
import dev.isxander.controlify.bindings.IBind;
import dev.isxander.controlify.gui.ButtonGuideRenderer;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.AbstractButtonComponentProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(AbstractButton.class)
public abstract class AbstractButtonMixin extends AbstractWidgetMixin implements ButtonGuideRenderer<AbstractButton> {
    @Unique private RenderData<AbstractButton> renderData = null;

    @Inject(method = "renderString", at = @At("RETURN"))
    private void renderButtonGuide(PoseStack matrices, Font renderer, int color, CallbackInfo ci) {
        if (shouldRender()) {
            switch (renderData.position()) {
                case LEFT -> getBind().render(matrices, getX() - getBind().size().width() - 1, getY() + getHeight() / 2);
                case RIGHT -> getBind().render(matrices, getX() + getWidth() + 1, getY() + getHeight() / 2);
                case TEXT -> {
                    Font font = Minecraft.getInstance().font;
                    int x;
                    if (font.width(getMessage()) > getWidth()) {
                        x = getX();
                    } else {
                        x = getX() + getWidth() / 2 - font.width(getMessage()) / 2 - getBind().size().width();
                    }

                    getBind().render(matrices, x, getY() + getHeight() / 2);
                }
            }
        }
    }

    @Inject(method = "renderString", at = @At("HEAD"))
    private void shiftXOffset(PoseStack matrices, Font renderer, int color, CallbackInfo ci) {
        matrices.pushPose();
        if (!shouldRender() || Minecraft.getInstance().font.width(getMessage()) > getWidth() || renderData.position() != ButtonRenderPosition.TEXT) return;
        matrices.translate(getBind().size().width() / 2f, 0, 0);
    }

    @Inject(method = "renderString", at = @At("RETURN"))
    private void finishShiftXOffset(PoseStack matrices, Font renderer, int color, CallbackInfo ci) {
        matrices.popPose();
    }

    @Override
    protected int shiftDrawSize(int x) {
        if (!shouldRender() || Minecraft.getInstance().font.width(getMessage()) < getWidth() || renderData.position() != ButtonRenderPosition.TEXT) return x;
        return x + getBind().size().width();
    }

    @Override
    public void setButtonGuide(RenderData<AbstractButton> renderData) {
        this.renderData = renderData;
    }

    private boolean shouldRender() {
        return renderData != null
                && this.isActive()
                && Controlify.instance().currentInputMode() == InputMode.CONTROLLER
                && Controlify.instance().currentController().config().showScreenGuide
                && !renderData.binding().apply(Controlify.instance().currentController().bindings()).isUnbound()
                && renderData.renderPredicate().shouldDisplay((AbstractButton) (Object) this);
    }

    private BindRenderer getBind() {
        return renderData.binding().apply(Controlify.instance().currentController().bindings()).renderer();
    }
}
