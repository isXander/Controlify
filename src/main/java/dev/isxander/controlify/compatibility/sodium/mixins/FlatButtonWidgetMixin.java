//? if sodium {
package dev.isxander.controlify.compatibility.sodium.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.gui.ButtonGuideRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.jellysquid.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Mixin(FlatButtonWidget.class)
public class FlatButtonWidgetMixin implements ButtonGuideRenderer<FlatButtonWidget> {
    @Shadow
    private boolean enabled;
    @Shadow
    private boolean visible;

    @Unique
    private RenderData<FlatButtonWidget> renderData = null;
    @Unique
    private final Map<InputBinding, Component> controllerMessages = new Object2ObjectArrayMap<>(2);

    @ModifyExpressionValue(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lme/jellysquid/mods/sodium/client/gui/widgets/FlatButtonWidget;label:Lnet/minecraft/network/chat/Component;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Component modifyRenderedLabel(Component actualLabel) {
        return getControllerMessage(actualLabel);
    }

    @Inject(method = "setLabel", at = @At("HEAD"))
    private void removeLabelCache(CallbackInfo ci) {
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
                && enabled
                && visible
                && renderData.shouldRender((FlatButtonWidget) (Object) this);
    }

    @Unique
    private Optional<InputBinding> getBind() {
        if (renderData == null) return Optional.empty();
        return renderData.getBind();
    }

    @Override
    public void controlify$setButtonGuide(RenderData<FlatButtonWidget> renderData) {
        this.renderData = renderData;
        this.controllerMessages.clear();
    }
}
//?}
