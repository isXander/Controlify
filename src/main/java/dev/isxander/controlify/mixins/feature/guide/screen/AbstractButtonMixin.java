package dev.isxander.controlify.mixins.feature.guide.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.bind.BindRenderer;
import dev.isxander.controlify.api.buttonguide.ButtonRenderPosition;
import dev.isxander.controlify.font.BindingFontHelper;
import dev.isxander.controlify.gui.ButtonGuideRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AbstractButton.class)
public abstract class AbstractButtonMixin extends AbstractWidgetMixin implements ButtonGuideRenderer<AbstractButton>, NarratableEntry /* isActive() in AbstractWidgetMixin shadow breaks dev env remapping. must re-implement interface */ {
    @Unique private RenderData<AbstractButton> renderData = null;
    @Unique private Component controllerMessage;

    @Override
    protected Component modifyMessage(Component actualMessage) {
        if (shouldRender()) {
            if (controllerMessage == null) updateControllerMessage();
            return controllerMessage;
        }
        return actualMessage;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void setControllerMessageInitially(int i, int j, int k, int l, Component component, CallbackInfo ci) {
        updateControllerMessage();
    }

    @Override
    protected void catchMessageSet(Component message, CallbackInfo ci) {
        updateControllerMessage();
    }

    @Override
    public void setButtonGuide(RenderData<AbstractButton> renderData) {
        this.renderData = renderData;
    }

    @Unique
    private void updateControllerMessage() {
        getBind().ifPresentOrElse(bind -> {
            var component = Component.empty();
            if (!Minecraft.getInstance().font.isBidirectional())
                component.append(BindingFontHelper.binding(bind));
            component.append(getActualMessage());
            if (Minecraft.getInstance().font.isBidirectional())
                component.append(BindingFontHelper.binding(bind));
            controllerMessage = component;
        }, () -> controllerMessage = null);
    }

    @Unique
    private boolean shouldRender() {
        return renderData != null
                && this.isActive()
                && getBind().isPresent()
                && Controlify.instance().currentInputMode().isController()
                && Controlify.instance().getCurrentController().map(c -> c.genericConfig().config().showScreenGuides).orElse(false)
                && !renderData.binding().onController(Controlify.instance().getCurrentController().orElseThrow()).isUnbound()
                && renderData.renderPredicate().shouldDisplay((AbstractButton) (Object) this);
    }

    @Unique
    private Optional<ResourceLocation> getBind() {
        if (renderData == null) return Optional.empty();
        return Controlify.instance().getCurrentController().map(c -> renderData.binding().onController(c).id());
    }
}
