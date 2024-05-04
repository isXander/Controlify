package dev.isxander.controlify.mixins.feature.guide.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.font.BindingFontHelper;
import dev.isxander.controlify.gui.ButtonGuideRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Mixin(AbstractButton.class)
public abstract class AbstractButtonMixin extends AbstractWidgetMixin implements ButtonGuideRenderer<AbstractButton>, NarratableEntry /* isActive() in AbstractWidgetMixin shadow breaks dev env remapping. must re-implement interface */ {
    @Unique private RenderData<AbstractButton> renderData = null;

    @Unique private final Map<ControllerBinding, Component> controllerMessages = new Object2ObjectArrayMap<>(2);

    @Override
    protected Component modifyRenderedMessage(Component actualMessage) {
        return getControllerMessage();
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
    private Component getControllerMessage() {
        if (!shouldRender())
            return getMessage();

        return getBind().map(bind -> controllerMessages.computeIfAbsent(bind, b -> {
            var component = Component.empty();
            if (!Minecraft.getInstance().font.isBidirectional()) {
                component.append(BindingFontHelper.binding(bind.id()));
                component.append(" ");
            }
            component.append(getMessage());
            if (Minecraft.getInstance().font.isBidirectional()) {
                component.append(" ");
                component.append(BindingFontHelper.binding(bind.id()));
            }
            return component;
        })).orElse(getMessage());
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
    private Optional<ControllerBinding> getBind() {
        if (renderData == null) return Optional.empty();
        return Controlify.instance().getCurrentController().map(c -> renderData.binding().onController(c));
    }
}
