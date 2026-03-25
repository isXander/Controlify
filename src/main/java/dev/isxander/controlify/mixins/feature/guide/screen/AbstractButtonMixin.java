package dev.isxander.controlify.mixins.feature.guide.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.gui.ButtonGuideRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Mixin(AbstractButton.class)
public abstract class AbstractButtonMixin extends AbstractWidgetMixin implements ButtonGuideRenderer<AbstractButton> {
    @Unique private RenderData<AbstractButton> renderData = null;
    @Unique private final Map<InputBinding, Component> controllerMessages = new Object2ObjectArrayMap<>(2);

    @ModifyExpressionValue(method = "extractDefaultLabel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractButton;getMessage()Lnet/minecraft/network/chat/Component;"))
    protected Component modifyRenderedLabel(Component originalMessage) {
        return getControllerMessage(originalMessage);
    }

    @Override
    protected void catchMessageSet(Component message, CallbackInfo ci) {
        controllerMessages.clear();
    }

    @Override
    public void controlify$setButtonGuide(RenderData<AbstractButton> renderData) {
        this.renderData = renderData;
        this.controllerMessages.clear();
    }

    @Unique
    private Component getControllerMessage(Component actualLabel) {
        if (!shouldRender())
            return actualLabel;

        return getBind().map(bind -> controllerMessages
                        .computeIfAbsent(bind, b -> renderData.getControllerMessage(b, actualLabel)))
                .orElse(actualLabel);
    }

    @Unique
    protected boolean shouldRender() {
        return renderData != null
                && isActive()
                && renderData.shouldRender((AbstractButton) (Object) this);
    }

    @Unique
    private Optional<InputBinding> getBind() {
        if (renderData == null) return Optional.empty();
        return renderData.getBind();
    }
}
