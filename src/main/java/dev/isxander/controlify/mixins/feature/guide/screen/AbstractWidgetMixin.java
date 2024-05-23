package dev.isxander.controlify.mixins.feature.guide.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.gui.ButtonGuideRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin implements ButtonGuideRenderer<AbstractWidget>, NarratableEntry {

    @Shadow public abstract int getX();

    @Shadow public abstract int getY();

    @Shadow public abstract int getHeight();

    @Shadow public abstract int getWidth();

    @Shadow
    public abstract Component getMessage();

    @Shadow
    public abstract boolean isActive();

    @Unique
    private RenderData<AbstractWidget> renderData = null;

    @Unique private final Map<InputBinding, Component> controllerMessages = new Object2ObjectArrayMap<>(2);

    @Inject(method = "setMessage", at = @At("RETURN"))
    protected void catchMessageSet(Component message, CallbackInfo ci) {
        controllerMessages.clear();
    }

    @ModifyExpressionValue(method = "renderScrollingString(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractWidget;getMessage()Lnet/minecraft/network/chat/Component;"))
    protected Component modifyRenderedMessage(Component actualMessage) {
        return getControllerMessage();
    }

    @Override
    public void controlify$setButtonGuide(RenderData<AbstractWidget> renderData) {
        this.renderData = renderData;
        this.controllerMessages.clear();
    }

    @Unique
    private Component getControllerMessage() {
        if (!shouldRender())
            return getMessage();

        return getBind().map(bind -> controllerMessages.computeIfAbsent(bind, b -> {
            var component = Component.empty();
            if (!Minecraft.getInstance().font.isBidirectional()) {
                component.append(bind.inputIcon());
                component.append(" ");
            }
            component.append(getMessage());
            if (Minecraft.getInstance().font.isBidirectional()) {
                component.append(" ");
                component.append(bind.inputIcon());
            }
            return component;
        })).orElse(getMessage());
    }

    @Unique
    protected boolean shouldRender() {
        return renderData != null
                && isActive()
                && getBind().isPresent()
                && Controlify.instance().currentInputMode().isController()
                && Controlify.instance().getCurrentController().map(c -> c.genericConfig().config().showScreenGuides).orElse(false)
                && !renderData.binding().get().on(Controlify.instance().getCurrentController().orElseThrow()).isUnbound()
                && renderData.renderPredicate().shouldDisplay((AbstractButton) (Object) this);
    }

    @Unique
    private Optional<InputBinding> getBind() {
        if (renderData == null) return Optional.empty();
        return Controlify.instance().getCurrentController().map(c -> renderData.binding().get().on(c));
    }
}
