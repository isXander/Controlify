package dev.isxander.controlify.mixins.feature.guide.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.bind.BindRenderer;
import dev.isxander.controlify.api.buttonguide.ButtonRenderPosition;
import dev.isxander.controlify.gui.ButtonGuideRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButton.class)
public abstract class AbstractButtonMixin extends AbstractWidgetMixin implements ButtonGuideRenderer<AbstractButton>, NarratableEntry /* isActive() in AbstractWidgetMixin shadow breaks dev env remapping. must re-implement interface */ {
    @Unique private RenderData<AbstractButton> renderData = null;

    @Inject(method = "renderString", at = @At("RETURN"))
    private void renderButtonGuide(GuiGraphics graphics, Font renderer, int color, CallbackInfo ci) {
        if (shouldRender()) {
            switch (renderData.position()) {
                case LEFT -> getBind().render(graphics, getX() - getBind().size().width() - 1, getY() + getHeight() / 2);
                case RIGHT -> getBind().render(graphics, getX() + getWidth() + 1, getY() + getHeight() / 2);
                case TEXT -> {
                    Font font = Minecraft.getInstance().font;
                    int x;
                    if (font.width(getMessage()) > getWidth()) {
                        x = getX();
                    } else {
                        x = getX() + getWidth() / 2 - font.width(getMessage()) / 2 - getBind().size().width();
                    }

                    getBind().render(graphics, x, getY() + getHeight() / 2);
                }
            }
        }
    }

    @Inject(method = "renderString", at = @At("HEAD"))
    private void shiftXOffset(GuiGraphics graphics, Font renderer, int color, CallbackInfo ci) {
        graphics.pose().pushPose();
        if (!shouldRender() || Minecraft.getInstance().font.width(getMessage()) > getWidth() || renderData.position() != ButtonRenderPosition.TEXT) return;
        graphics.pose().translate(getBind().size().width() / 2f, 0, 0);
    }

    @Inject(method = "renderString", at = @At("RETURN"))
    private void finishShiftXOffset(GuiGraphics graphics, Font renderer, int color, CallbackInfo ci) {
        graphics.pose().popPose();
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
                && Controlify.instance().currentInputMode().isController()
                && Controlify.instance().getCurrentController().map(c -> c.genericConfig().config().showScreenGuides).orElse(false)
                && !renderData.binding().onController(Controlify.instance().getCurrentController().orElseThrow()).isUnbound()
                && renderData.renderPredicate().shouldDisplay((AbstractButton) (Object) this);
    }

    private BindRenderer getBind() {
        return renderData.binding().onController(Controlify.instance().getCurrentController().orElseThrow()).renderer();
    }
}
